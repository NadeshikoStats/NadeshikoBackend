## The backend server for [Nadeshiko.io](https://nadeshiko.io) ([GitHub](https://github.com/NadeshikoStats))

## Features

### An API providing player information and Hypixel stats
`/stats`: Pararms: `name`. Example response (truncated):

 ```json
{
  "success": true,
  "name": "username",
  "uuid": "00000000-0000-0000-0000-000000000000",
  "skin": "http://textures.minecraft.net/texture/...",
  "slim": true,
  "cape": "http://textures.minecraft.net/texture/...",
  "status": {},
  "guild": {},
  "profile": {},
  "stats": {},
  "achievements": {}
```

### An API providing real-time customizable stat cards
`/card/data`: Params: `data`: URL-safe Base64 encoded JSON including `name`, `game`, and `size` fields. Example:
 
#### Request: 

```json
{
  "name": "heatran",
  "game": "NETWORK",
  "size": "FULL"
}
```

`https://nadeshiko.io/card/eyJuYW1lIjoiaGVhdHJhbiIsImdhbWUiOiJORVRXT1JLIiwic2l6ZSI6IkZVTEwifQ==`

#### Response:

![image](https://github.com/NadeshikoStats/NadeshikoBackend/assets/146425360/d4221c40-530b-4fac-974a-95926e72447b)

### Real-time customizable logging and monitoring via Discord webhooks

![image](https://github.com/NadeshikoStats/NadeshikoBackend/assets/146425360/82c9c002-031e-4d95-b60d-3ed1265b009f)
![image](https://github.com/NadeshikoStats/NadeshikoBackend/assets/146425360/90d336c5-26c4-46e3-8d04-b3c9eedf96d8)


## Running

### Configuration

Upon starting, the server will search the current working directory for `config.json`. This file defines the Hypixel API key used, the port to bind the server to, and the settings for the built-in Discord monitoring. 
This file must be present for the server to start.

If the configuration file is not found, the server will exit with the following message:
> 21:12:19.736 [main/ERROR] io.nadeshiko.nadeshiko.Nadeshiko: No config.json was found! Halting.

An example configuation file is provided below:
```json
{
    "hypixel_key": "your_api_key",
    "port": 2000,
    "discord": {
        "enabled": true,
        "log_url": "https://discord.com/api/webhooks/foo/bar",
        "alert_url": "https://discord.com/api/webhooks/foo/bar"
    }
}
```

If `port` is missing, the server will default to binding to port 2000. If the `discord` object is missing, the server will disable the Discord monitoring system gracefully, the same as if the Discord object's `enabled` field were set to false. 
The `hypixel_api` field must be present with a valid API key for the server to start.

### Building and Starting

Build the server via `> mvn package`

Place the compiled JAR in the directory with the configuation file (see above)

Start the server via `> java -jar Nadeshiko-VERSION.jar`

The server should start up, eventually printing the message: 
> 21:26:03.977 [main/INFO] io.nadeshiko.nadeshiko.Nadeshiko: Nadeshiko is now up! Took 0.68 seconds to ignite!

From this point on, the server should be available on the port specified in `config.json`, or 2000 if no port was specified. Visiting `localhost:port` should show a page displaying the version information of the server.
