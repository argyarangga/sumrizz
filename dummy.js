const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');
const { Translate } = require('@google-cloud/translate').v2;
const { extract } = require('youtube-extract');
const fs = require('fs');
const { Whisper } = require('whisper-node');

const app = express();
const api = express.Router();

const PATTERN_URL = /^(https?:\/\/)?(www\.)?(youtube|youtu|youtube-nocookie)\.(com|be)\/(watch\?v=|embed\/|v\/|.+\?v=)?([^&=%\?]{11})/;

const OUTPUT_PATH = 'YoutubeAudios';
const API_KEY = 'AIzaSyClq5D1GB7EzUewH9j0ChZcGSSAS8Ls6vA';

app.use(bodyParser.json());

class Transcript {
  constructor() {
    this.data = {};
  }

  urlPathValidation(url) {
    const patternUrlMatch = PATTERN_URL.test(url);
    return patternUrlMatch;
  }

  async isVideoAvailable(apiKey, videoId) {
    try {
      const response = await axios.get(
        `https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=status&key=${apiKey}`
      );

      const items = response.data.items;
      if (items && items.length > 0) {
        const uploadStatus = items[0].status.uploadStatus;
        return uploadStatus === 'processed';
      } else {
        return false;
      }
    } catch (error) {
      console.error(error);
      return false;
    }
  }

  async transcribeYoutubeVideo(ytVideoId) {
    try {
      const response = await axios.get(
        `https://www.googleapis.com/youtube/v3/captions?videoId=${ytVideoId}&part=snippet&key=${API_KEY}`
      );

      const captions = response.data.items;
      const text = captions.map((caption) => caption.snippet.text).join(' ');
      return text;
    } catch (error) {
      console.error(error);
      return error.message;
    }
  }

  async translateText(text, targetLanguage) {
    const translate = new Translate();
    const [translation] = await translate.translate(text, targetLanguage);
    return translation;
  }

  async downloadAudioStream(url) {
    try {
      const videoId = extract(url);
      const audioStream = await Whisper.extract(url, { quality: 'highestaudio' });
      const filename = `audio_${videoId}.mp3`;
      await audioStream.pipe(fs.createWriteStream(`${OUTPUT_PATH}/${filename}`));
      console.log('Audio downloaded successfully!');
      this.data = { source: url, filename, id: videoId, output_path: OUTPUT_PATH };
    } catch (error) {
      console.error(error);
      throw error;
    }
  }

  async transcriptAudio() {
    const model = new Whisper('base');
    const audioPath = `${OUTPUT_PATH}/audio_${this.data.id}.mp3`;
    const result = await model.transcribe(audioPath, { fp16: false });
    return result.text;
  }

  async post(req, res) {
    const { source, type, language } = req.body;
    this.data = { source, type, language };

    try {
      if (type === 'link') {
        if (!this.urlPathValidation(source)) {
          return res.status(400).json({ error: 'Enter the correct YouTube source' });
        }

        const videoId = extract(source);
        this.data.id = videoId;

        if (!(await this.isVideoAvailable(API_KEY, videoId))) {
          return res.status(400).json({ error: 'Video doesn\'t exist anymore' });
        }

        let transcription = await this.transcribeYoutubeVideo(videoId);

        if (transcription === 'Transcription not available') {
          await this.downloadAudioStream(source);
          transcription = await this.transcriptAudio();
        }

        this.data.transcription = transcription;

        const translateTranscription = await this.translateText(
          this.data.transcription,
          this.data.language
        );

        const cleanTranslateTranscription = translateTranscription.replace(/&#39;/g, "'");
        this.data[`${this.data.language}_transcription`] = cleanTranslateTranscription;

        return res.status(200).json({ status: 'Success', body: this.data });
      } else if (type === 'text') {
        if (source.startsWith('http') || source.startsWith('www') || !source.trim()) {
          return res.status(400).json({ error: 'Enter the appropriate data' });
        }

        if (/^https?:\/\//.test(source)) {
          return res.status(400).json({ error: 'Enter the appropriate data' });
        }

        const enTranscription = await this.translateText(source, 'en');
        this.data.en_transcription = enTranscription;

        return res.status(200).json({ status: 'Success', body: this.data });
      } else {
        return res.status(400).json({ error: 'Invalid type' });
      }
    } catch (error) {
      console.error(error);
      return res.status(500).json({ error: error.message || 'Internal Server Error' });
    }
  }
}

const transcript = new Transcript();

api.post('/trans', (req, res) => transcript.post(req, res));

app.use('/api', api);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
