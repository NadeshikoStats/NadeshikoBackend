package io.nadeshiko.nadeshiko.monitoring;

import io.nadeshiko.nadeshiko.Nadeshiko;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DiscordMonitor {

	private static final Color COLOR_OK = new Color(128, 255, 128);
	private static final Color COLOR_LOG = new Color(128, 128, 128);
	private static final Color COLOR_ALERT = new Color(255, 128, 128);

	private final String logUrl;
	private final String alertUrl;

	/**
	 * The Discord monitor is optional, and is disabled automatically if either webhook URL is null
	 */
	private boolean enabled;

	public DiscordMonitor(String logUrl, String alertUrl) {

		this.enabled = logUrl != null && alertUrl != null;

		this.logUrl = logUrl;
		this.alertUrl = alertUrl;
	}

	/**
	 * Sends a positive message to the webhook defined by {@code logWebhook}
	 *
	 * @param message The message to log, including {@link String#format(String, Object...)} formatting codes
	 * @param args Optional arguments for the formatting codes provided in {@code message}
	 */
	public void ok(Object message, Object... args) {

		if (!this.enabled) {
			return;
		}

		String formattedMessage = String.format(message.toString(), args);

		long unixSeconds = System.currentTimeMillis() / 1000;
		String prefix = String.format("<t:%d:f>: ", unixSeconds);

		// Kinda hacky, but get the name of the class that called for this log entry
		String author = String.format("%s says:", Thread.currentThread().getStackTrace()[2].getClassName());

		DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
		embed.setAuthor(author, null, null);
		embed.setColor(COLOR_OK);
		embed.setDescription(prefix + formattedMessage);
		embed.setFooter("Sent from Nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		// Send the embed to the webhook
		try {
			DiscordWebhook logWebhook = new DiscordWebhook(logUrl);
			logWebhook.addEmbed(embed);
			logWebhook.execute();
		} catch (IOException e) {
			Nadeshiko.logger.error("Failed to log to the logging webhook! Is the URL valid?");
			this.enabled = false; // Disable the monitor if we find it to be using an invalid URL
		}
	}

	/**
	 * Sends a neutral message to the webhook defined by {@code logWebhook}
	 *
	 * @param message The message to log, including {@link String#format(String, Object...)} formatting codes
	 * @param args Optional arguments for the formatting codes provided in {@code message}
	 */
	public void log(Object message, Object... args) {

		if (!this.enabled) {
			return;
		}

		String formattedMessage = String.format(message.toString(), args);

		long unixSeconds = System.currentTimeMillis() / 1000;
		String prefix = String.format("<t:%d:f>: ", unixSeconds);

		// Kinda hacky, but get the name of the class that called for this log entry
		String author = String.format("%s says:", Thread.currentThread().getStackTrace()[2].getClassName());

		DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
		embed.setAuthor(author, null, null);
		embed.setColor(COLOR_LOG);
		embed.setDescription(prefix + formattedMessage);
		embed.setFooter("Sent from Nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		// Send the embed to the webhook
		try {
			DiscordWebhook logWebhook = new DiscordWebhook(logUrl);
			logWebhook.addEmbed(embed);
			logWebhook.execute();
		} catch (IOException e) {
			Nadeshiko.logger.error("Failed to log to the logging webhook! Is the URL valid?");
			this.enabled = false; // Disable the monitor if we find it to be using an invalid URL
		}
	}

	public void alertException(Exception e, Object message, Object... args) {
		String formattedMessage = String.format(message.toString(), args);

		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();

		String stackTrace = writer.toString().
			replace("\n", "\\n").
			replace("\t", "    ").
			replace("\"", "\\\"");

		String finalMessage = String.format("%s.\\n\\n**Stack Trace:**\\n```\\n%s\\n```", formattedMessage, stackTrace);

		long unixSeconds = System.currentTimeMillis() / 1000;
		String prefix = String.format("<t:%d:f>: ", unixSeconds);

		// Kinda hacky, but get the name of the class that called for this log entry
		String author = String.format("%s says:", Thread.currentThread().getStackTrace()[2].getClassName());

		DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
		embed.setAuthor(author, null, null);
		embed.setColor(COLOR_ALERT);
		embed.setDescription(prefix + finalMessage);
		embed.setFooter("Sent from Nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		// Send the embed to the webhook
		try {
			DiscordWebhook alertWebhook = new DiscordWebhook(alertUrl);
			alertWebhook.addEmbed(embed);
			alertWebhook.execute();
		} catch (IOException e2) {
			Nadeshiko.logger.error("Failed to log to the alert webhook! Is the URL valid?");
			this.enabled = false; // Disable the monitor if we find it to be using an invalid URL
		}
	}

	/**
	 * Sends a negative message to the webhook defined by {@code alertWebhook}
	 *
	 * @param message The message to log, including {@link String#format(String, Object...)} formatting codes
	 * @param args Optional arguments for the formatting codes provided in {@code message}
	 */
	public void alert(Object message, Object... args) {

		if (!this.enabled) {
			return;
		}

		String formattedMessage = String.format(message.toString(), args);

		long unixSeconds = System.currentTimeMillis() / 1000;
		String prefix = String.format("<t:%d:f>: ", unixSeconds);

		// Kinda hacky, but get the name of the class that called for this log entry
		String author = String.format("%s says:", Thread.currentThread().getStackTrace()[2].getClassName());

		DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
		embed.setAuthor(author, null, null);
		embed.setColor(COLOR_ALERT);
		embed.setDescription(prefix + formattedMessage);
		embed.setFooter("Sent from Nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		// Send the embed to the webhook
		try {
			DiscordWebhook alertWebhook = new DiscordWebhook(alertUrl);
			alertWebhook.addEmbed(embed);
			alertWebhook.execute();
		} catch (IOException e) {
			Nadeshiko.logger.error("Failed to log to the alert webhook! Is the URL valid?");
			this.enabled = false; // Disable the monitor if we find it to be using an invalid URL
		}
	}
}
