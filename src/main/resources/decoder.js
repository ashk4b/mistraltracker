/**
 * Entry, decoder.js
 */
function decodeUplink (input, port) {
  var bytes = input['bytes'];

  bytes = bytes2HexString(bytes).toLocaleUpperCase();

  let result = {
    'err': 0, 'payload': bytes, 'valid': true, messages: []
  }
  let splitArray = dataSplit(bytes)

  let decoderArray = []
  for (let i = 0; i < splitArray.length; i++) {
    let item = splitArray[i]
    let dataId = item.dataId
    let dataValue = item.dataValue
    let messages = dataIdAndDataValueJudge(dataId, dataValue)
    if (Array.isArray(messages)) {
      for(let j=0; j<messages.length; j++) {
        decoderArray.push(messages[j]);
      }
    } else {
      decoderArray.push(messages);
    }
  }
  result.messages = decoderArray
  return { data: result }
}

function dataSplit (bytes) {
  let frameArray = []
  if (!bytes || bytes.length < 2) return frameArray;

  for (let i = 0; i < bytes.length; i++) {
    let remainingValue = bytes
    let dataId = remainingValue.substring(0, 2).toLowerCase()
    let dataValue
    let dataObj = {}

    switch (dataId) {
      case '01' : case '20' : case '21' : case '30' : case '31' : case '33' : case '40' : case '41' : case '42' : case '43' : case '44' : case '45' : case '4a' :
        dataValue = remainingValue.substring(2, 22)
        bytes = remainingValue.substring(22)
        break
      case '02': case '4b':
        dataValue = remainingValue.substring(2, 18)
        bytes = remainingValue.substring(18)
        break
      case '03' : case '06':
        dataValue = remainingValue.substring(2, 4)
        bytes = remainingValue.substring(4)
        break
      case '05' : case '34':
        dataValue = bytes.substring(2, 10)
        bytes = remainingValue.substring(10)
        break
      case '04': case '10': case '32': case '35': case '36': case '37': case '38': case '39':
        dataValue = bytes.substring(2, 20)
        bytes = remainingValue.substring(20)
        break
      case '4c':
        dataValue = bytes.substring(2, 14)
        bytes = remainingValue.substring(14)
        break
      default:
        dataValue = '9'
        break
    }

    if (dataValue === '9' || !dataValue || dataValue.length < 2) {
      break
    }

    frameArray.push({ 'dataId': dataId, 'dataValue': dataValue })
  }
  return frameArray
}

function dataIdAndDataValueJudge (dataId, dataValue) {
  let messages = []
  switch (dataId) {
    case '01':
    case '4a':
      messages = [
        { measurementValue: loraWANV2DataFormat(dataValue.substring(0, 4), 10), type: 'Air Temperature' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(4, 6)), type: 'Air Humidity' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(6, 14)), type: 'Light Intensity' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(14, 16), 10), type: 'UV Index' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(16, 20), 10), type: 'Wind Speed' }
      ]
      break
    case '02':
    case '4b':
      messages = [
        { measurementValue: loraWANV2DataFormat(dataValue.substring(0, 4)), type: 'Wind Direction Sensor' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(4, 12), 1000), type: 'Rain Gauge' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(12, 16), 0.1), type: 'Barometric Pressure' }
      ]
      break
    case '03':
      messages = [{ 'Battery(%)': loraWANV2DataFormat(dataValue) }]
      break
    case '4c':
      messages = [
        { measurementValue: loraWANV2DataFormat(dataValue.substring(0, 4), 10), type: 'Peak Wind Gust' },
        { measurementValue: loraWANV2DataFormat(dataValue.substring(4, 12), 1000), type: 'Rain Accumulation' }
      ]
      break
  }
  return messages
}

function loraWANV2DataFormat (str, divisor = 1) {
  let strReverse = bigEndianTransform(str)
  let str2 = toBinary(strReverse)
  if (str2.substring(0, 1) === '1') {
    let arr = str2.split('')
    let reverseArr = arr.map((item) => {
      return parseInt(item) === 1 ? 0 : 1
    })
    str2 = parseInt(reverseArr.join(''), 2) + 1
    return parseFloat('-' + str2 / divisor)
  }
  return parseInt(str2, 2) / divisor
}

function bigEndianTransform (data) {
  let dataArray = []
  for (let i = 0; i < data.length; i += 2) {
    dataArray.push(data.substring(i, i + 2))
  }
  return dataArray
}

function toBinary (arr) {
  let binaryData = arr.map((item) => {
    let data = parseInt(item, 16).toString(2)
    let dataLength = data.length
    if (data.length !== 8) {
      for (let i = 0; i < 8 - dataLength; i++) {
        data = `0` + data
      }
    }
    return data
  })
  return binaryData.toString().replace(/,/g, '')
}

function bytes2HexString (arrBytes) {
  var str = ''
  for (var i = 0; i < arrBytes.length; i++) {
    var tmp
    var num = arrBytes[i]
    if (num < 0) {
      tmp = (255 + num + 1).toString(16)
    } else {
      tmp = num.toString(16)
    }
    if (tmp.length === 1) {
      tmp = '0' + tmp
    }
    str += tmp
  }
  return str
}