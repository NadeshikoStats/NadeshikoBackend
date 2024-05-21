/*
 * This file is a part of the Nadeshiko project. Nadeshiko is free software, licensed under the MIT license.
 *
 * Usage of these works (including, yet not limited to, reuse, modification, copying, distribution, and selling) is
 * permitted, provided that the relevant copyright notice and permission notice (as specified in LICENSE) shall be
 * included in all copies or substantial portions of this software.
 *
 * These works are provided "AS IS" with absolutely no warranty of any kind, either expressed or implied.
 *
 * You should have received a copy of the MIT License alongside this software; refer to LICENSE for information.
 * If not, refer to https://mit-license.org.
 */

package io.nadeshiko.nadeshiko.monitoring;

import io.nadeshiko.nadeshiko.Nadeshiko;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class to support rich logging to a Discord webhook for remote monitoring
 * @author chloe
 */
public class DiscordMonitor {

	private static final Color COLOR_OK = new Color(128, 255, 128);
	private static final Color COLOR_LOG = new Color(128, 128, 128);
	private static final Color COLOR_ALERT = new Color(255, 128, 128);

	/**
	 * The webhook URL to log to for most messages
	 */
	private final String logUrl;

	/**
	 * The webhook URL to log to for severe messages/alerts
	 */
	private final String alertUrl;

	/**
	 * Whether the Discord monitor is enabled or not
	 */
	private boolean enabled;

	/**
	 * Constructs a new DiscordMonitor from the provided webhook URLs
	 * @param logUrl The webhook URL to log to for most messages
	 * @param alertUrl The webhook URL to log to for severe messages/alerts
	 */
	public DiscordMonitor(String logUrl, String alertUrl) {

		// Disable the monitor if either URL is missing
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
