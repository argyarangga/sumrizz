const express = require("express")
const route = require("./index")
const morgan = require('morgan');
const bodyParser = require('body-parser');
const cors = require('cors');

require('dotenv').config()

const app = express()
const port = 3000

app.use(morgan('dev'))
app.use(cors())
app.use(express.urlencoded({extended : true}))
app.use(bodyParser.json())

// ====Line ini kebawah jangan dirubah====
app.use('/', route)

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})
