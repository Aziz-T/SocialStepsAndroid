var request = require("request");

const CLIENT_ID = "106834575";
const CLIENT_SECRET = "d381358cd4ba5bc8a3942baead1717b2a6fc96b0cb5409869c48e3b2cce4ab76";

var options = {
  method: "POST",
  url: "https://oauth-login.cloud.huawei.com/oauth2/v3/token",
  headers: {
    "Content-Type": "application/x-www-form-urlencoded",
  },
  form: {
    grant_type: "client_credentials",
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
  },
};

const getPushOptions = (accessToken, eventObject) => {
    let mToken = eventObject.mToken;
    let photoUrl = eventObject.photoUrl;
    let content = eventObject.content;
  return {
    method: "POST",
    url: "https://push-api.cloud.huawei.com/v1/" + CLIENT_ID + "/messages:send",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
    body: JSON.stringify({
      validate_only: false,
      message: {
        notification: {
          title: "Social Steps",
          body: content,
          image: photoUrl
        },
        android: {
          notification: {
            click_action: {
              type: 3,
            },
          },
        },
        token:  [mToken],
      },
    }),
  };
};

const obtainAccessToken = (callback) => {
  try {
    request(options, function (error, response) {
      if (error) {
        console.log(response.body);
        callback(error);
      } else {
        try {
          callback(JSON.parse(response.body).access_token);
        } catch (error) {
          callback(error);
        }
      }
    });
  } catch (error) {
    callback(error);
  }
};

const pushToken = (accessToken, eventObject, callback) => {
  const optionsForPush = getPushOptions(accessToken, eventObject);

  request(optionsForPush, function (error, response) {
    if (error) throw new Error(error);
    console.log(response.body);
    callback(response.body);
  });
};

let myHandler = function (event, context, callback, logger) {
  let eventObject = JSON.parse(event.body);
  try {
    obtainAccessToken((token) => {
      pushToken(token, eventObject, (msg) => {
        callback({ msg });
        console.log(msg);
        logger.info("Success.")
      });
    });
  } catch (error) {
    callback({ error });
    console.log(error);
    logger.error("Error: " + error);
  }
};

module.exports.myHandler = myHandler;