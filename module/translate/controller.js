const cheerio = require("cheerio");
const axios = require("axios");
const fs = require("fs");
const { Translate } = require("@google-cloud/translate").v2;

// ==== Inisialisasi Project ID yang sesuai dengan Credential ====
const projectId = "sumrizz-408115";
const translate = new Translate({ projectId });

class controller {
  static async inputText(req, res) {
    const { text, lang } = req.body;

    try {
      let [translation] = await translate.translate(text, `${lang}`);
      res.status(201).json({ translation: translation });
    } catch (error) {
      res.status(500).send(error);
    }
  }

  static async inputUrl(req, res) {
    const url = req.body.url;
    const response = await axios.getAdapter(url);

    let $ = cheerio.load(response.data);
    let text = $("body").text();
    try {
      let [translation] = await translate.translate(text, `en`);
      res.status(201).json({ translation: translation });
    } catch (error) {
      res.status(500).send(error)
    }
  }

  static async sukses(req, res) {
    res.status(200).send("Hello World!");
  }
}

module.exports = controller;
