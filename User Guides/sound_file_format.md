# Sound File Format

In order for a sound file to be able to be played on the Alexa app, it must be in a specific file format.
As such, it would be necessary for all parts of the application to adhere to these same requirements.

Here are the requirements:
* The MP3 must be hosted at an Internet-accessible HTTPS endpoint. HTTPS is required, and the domain hosting the MP3 file must present a valid, trusted SSL certificate. Self-signed certificates cannot be used.
* The MP3 must not contain any customer-specific or other sensitive information.
* The MP3 must be a valid MP3 file (MPEG version 2).
* The audio file cannot be longer than 240 seconds.
* The bit rate must be 48 kbps. Note that this bit rate gives a good result when used with spoken content, but is generally not a high enough quality for music.
* The sample rate must be 22050Hz, 24000Hz, or 16000Hz.
