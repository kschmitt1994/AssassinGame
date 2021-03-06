/******************************************************************************/
/* cloudfunctions/functions/index.js                                          */
/* last updated March 24, 2017                                                */
/* by Sam Roth                                                                */
/******************************************************************************/

'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

/**
 * This is a sample function that demonstrates how the Firebase Cloud
 * Messaging and Functions services work. You just visit the URL provided
 * in the Firebase console and you are greeted with the text below.
 */
exports.helloWorld = functions.https.onRequest((request, response) => {
  response.send('Hello from Firebase!');
});

/**
 * NOTE: The below method names should correspond exactly to their
 *       FirebaseHelper counterparts. The cloud functions should then be called
 *       from inside those helper methods.
 */

/* BEGIN CLOUD FUNCTIONS ******************************************************/

/**
 * This triggers a notification on the device of a player who has been invited
 * to a particular game. Acting on the notification will take them to a dialogue
 * where they have the opportunity to accept or decline the game invitation.
 */
exports.sendInvite = functions.database
  .ref('users/{inviteeID}/invites/{inviterID}/{gameID}').onWrite(event => {

  const inviteeID = event.params.inviteeID;
  const inviterID = event.params.inviterID;
  const gameID    = event.params.gameID;

  if (!event.data.val()) {
    // No invitation for this game exists (for whatever reason)
    return console.log('Nothing to see here folks');
  }

  // Just for our records
  console.log(inviterID + ' has invited ' + inviteeID + ' to ' + gameID);

  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database()
    .ref(`/users/${inviteeID}/device`).once('value');

  return Promise.all([getDeviceTokensPromise]).then(results => {
    const tokensSnapshot = results[0];

    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(),
                'tokens to send notifications to.');

    // Notification details.
    const payload = {
      data : {
        type: "invitation",
        sender: inviterID,
        receiver: inviteeID,
        game: gameID
      }
    };

    // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });
  });
});

/**
 * This is one of two invitation response handlers that are sent back to the
 * admin. NOTE: Both accept and decline are handled in this single function.
 */
exports.sendInviteResponse = functions.database
  .ref('games/{gameID}/invites/{invitedID}').onWrite(event => {

    const gameID    = event.params.gameID;
    const invitedID = event.params.invitedID;

    if (!event.data.val()) {
      // No invitation for this game exists (for whatever reason)
      return console.log('Nothing to see here folks');
    }

    // Just for our records
    console.log(invitedID + ' has responded to invite to join ' + gameID);

    // Get the list of device notification tokens.
    const getInvitationResponsePromise = admin.database()
      .ref(`games/${gameID}/invites/${invitedID}`).once('value');
    const getGameAdminPromise = admin.database()
      .ref(`games/${gameID}/admin`).once('value');

    return Promise.all([getInvitationResponsePromise,
                        getGameAdminPromise]).then(results => {
      const invitationResponseSnapshot = results[0];
      const adminUsernameSnapshot = results[1];
      const getGameAdminDeviceToken = admin.database()
        .ref(`users/${adminUsernameSnapshot.val()}/device`).once('value');
      return Promise.all([getGameAdminDeviceToken]).then(results => {
        const gameAdminDeviceToken = results[0];
        console.log("gameAdminDeviceToken = " + Object.keys(gameAdminDeviceToken));
        console.log("gameAdminDeviceToken.val() = " + gameAdminDeviceToken.val());

        var response;
        if (invitationResponseSnapshot.val() == 'accepted') {
          response = `${invitedID} accepted your invitation to join ${gameID}.`;
        } else if (invitationResponseSnapshot.val() == 'declined') {
          response = `${invitedID} declined your invitation to join ${gameID}.`;
        } else {
          return console.log('Invalid response from player');
        }

        // Notification details.
        const payload = {
          notification: {
            body: response
          },
          data: {
            type: "invite_response",
            player_name: invitedID
          }
        };

        // Listing all tokens.
        const tokens = Object.keys(gameAdminDeviceToken);

        // Send notifications to all tokens.
        return admin.messaging().sendToDevice(tokens, payload).then(response => {
          // For each message check if there was an error.
          const tokensToRemove = [];
          response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
              console.error('Failure sending notification to', tokens[index], error);
              // Cleanup the tokens who are not registered anymore.
              if (error.code === 'messaging/invalid-registration-token' ||
                  error.code === 'messaging/registration-token-not-registered') {
                tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
              }
            }
          });
        return Promise.all(tokensToRemove);
      });
    });
  });
});

/**
 * This is a notification that gets sent to all players associated with a
 * particular game. It is triggered when the game status is set to 'STARTED',
 * and informs the users that the game they've joined has begun.
 */
exports.sendGameStartMessage =  functions.database
  .ref('games/{gameID}/status').onWrite(event => {

    const gameID = event.params.gameID;
    if (!event.data.val()) {
      return console.log('No game status info');
    } else {
      const status = event.data.val();
      if (status == 'started' || status == 'STARTED') {

        admin.database().ref(`games/${gameID}/admin`).once('value', function(adminSnapshot) {
          const adminName = adminSnapshot.val();

          admin.database().ref(`games/${gameID}/players`).once('value', function(playersSnapshot) {
            playersSnapshot.forEach(function(playerSnapshot) {
              console.log(playerSnapshot.key); // returns player's username
              const payload = {
                data: {
                  type: "game_start",
                  admin: adminName,
                  player: playerSnapshot.key,
                  game: gameID
                }
              }; // payload

              admin.messaging().sendToDevice(tokens, payload).then(response => {
                // For each message check if there was an error.
                const tokensToRemove = [];
                response.results.forEach((result, index) => {
                  const error = result.error;
                  if (error) {
                    console.error('Failure sending notification to', tokens[index], error);
                    // Cleanup the tokens who are not registered anymore.
                    if (error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered') {
                      tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                    }
                  }
                }); // response.forEach()
              }); // admin.messaging()
            }); // playerSnapshot.forEach()
          });
        });

        // admin.database().ref(`games/${gameID}/players`).once('value', function(playersSnapshot) {
        //   playersSnapshot.forEach(function(playerSnapshot) {
        //     console.log(playerSnapshot.key); // returns player's username
        //     const payload = {
        //       data: {
        //         type: "game_start",
        //         admin: gameAdminSnapshot.val(),
        //         player: gamePlayersSnapshot.val(),
        //         game: gameID
        //       }
        //     }; // payload
        //   });
        // });
        // const gameAdminPromise = admin.database()
        //   .ref(`games/${gameID}/admin`).once('value');
        //
        //   return Promise.all([gamePlayerPromise, gameAdminPromise]).then(results => {
        //     const gamePlayersSnapshot = results[0];
        //     const gameAdminSnapshot = results[1];
        //     console.log(gameAdminSnapshot);
        //     console.log(Object.keys(gameAdminSnapshot));
        //     for (let player in gamePlayersSnapshot.val()) {
        //       const getPlayerDeviceToken = admin.database()
        //         .ref(`users/${player}/device`).once('value');
        //       Promise.all([getPlayerDeviceToken]).then(results => {
        //         const gameAdminDeviceToken = results[0];
        //
        //         // Notification details.
        //         const payload = {
        //           // notification: {
        //           //   body: `${gameID} has started!`
        //           // },
        //           data: {
        //             type: "game_start",
        //             admin: gameAdminSnapshot.val(),
        //             player: gamePlayersSnapshot.val(),
        //             game: gameID
        //           }
        //         };
        //
        //         // Listing all tokens.
        //         const tokens = Object.keys(gameAdminDeviceToken.val());
        //
        //         // Send notifications to all tokens.
        //         return admin.messaging().sendToDevice(tokens, payload).then(response => {
        //           // For each message check if there was an error.
        //           const tokensToRemove = [];
        //           response.results.forEach((result, index) => {
        //             const error = result.error;
        //             if (error) {
        //               console.error('Failure sending notification to', tokens[index], error);
        //               // Cleanup the tokens who are not registered anymore.
        //               if (error.code === 'messaging/invalid-registration-token' ||
        //                   error.code === 'messaging/registration-token-not-registered') {
        //                 tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
        //               }
        //             }
        //           });
        //         return Promise.all(tokensToRemove);
        //       });
        //   });
    } // END IF GAME STARTED
  } // end DATA VAL PRESENT
});

/**
 * This function is fired when a new player joins a game that has already
 * started. In that case, a notification is sent to all other players saying
 * that this person has joined their game.
 */
exports.newPlayerAddedUp = functions.database
.ref('games/{gameID}/players').onWrite(event => {

  const gameID = event.params.gameID;

  if (!event.data.val()) {
    return console.log('Player removed');
  }

  const newPlayerName = event.data.val();
  console.log(newPlayerName);

  const gamePlayerPromise = admin.database()
    .ref(`games/${gameID}/players`).once('value');

    Promise.all([gamePlayerPromise]).then(results => {
      const gamePlayersSnapshot = results[0];
      for (let player in gamePlayersSnapshot.val()) {
        // console.log('player: ' + player);
        // console.log('newPlayerName: ' + Object.keys(newPlayerName));
        // console.log(gamePlayersSnapshot.val());
        if (newPlayerName[player]) {
          // console.log('PLAYER THAT IS NOT NEW PLAYER: ' + player);
          const getPlayerDeviceToken = admin.database()
            .ref(`users/${player}/device`).once('value');
          return Promise.all([getPlayerDeviceToken]).then(results => {
            const gamePlayerDeviceTokens = results[0];

            // Notification details.
            const payload = {
              notification: {
                body: `${newPlayerName} has joined ${gameID}!`
              },
              data: {
                type: "new_player_joined",
                player_name: Object.keys(newPlayerName)
              }
            };

            // Listing all tokens.
            const tokens = Object.keys(gamePlayerDeviceTokens['A'].val());

            // Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response => {
              // For each message check if there was an error.
              const tokensToRemove = [];
              response.results.forEach((result, index) => {
                const error = result.error;
                if (error) {
                  console.error('Failure sending notification to', tokens[index], error);
                  // Cleanup the tokens who are not registered anymore.
                  if (error.code === 'messaging/invalid-registration-token' ||
                      error.code === 'messaging/registration-token-not-registered') {
                    // tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                  }
                }
              });
            return Promise.all(tokensToRemove);
          });
        });
        }
      } // end for loop
  });
});

exports.gameEndAlert = functions.database
.ref('games/{gameID}/result').onWrite(event => {

  const gameID = event.params.gameID;

  if (!event.data.val()) {
    return console.log('No result found');
  }

  const resultText = event.data.val();
  console.log("Game result: " + resultText);

  // Get device identifiers for all game players

  const gamePlayers = new Array();
  const deviceIdentifiers = new Array();
  const gamePlayerPromise = admin.database().ref(`games/${gameID}/players`).once('value');

  return Promise.resolve(gamePlayerPromise).then(results => {
    const gamePlayersResult = results;
    console.log("gamePlayersResult: " + gamePlayersResult);

    gamePlayersResult.forEach(function (gamePlayerSnapshot) {
      // console.log(gamePlayerSnapshot.key); // DEBUGGING

      admin.database().ref(`users/${gamePlayerSnapshot.key}/device`).once('value', function(playerDevicesSnapshot) {
        playerDevicesSnapshot.forEach(function(deviceSnapshot) {
          console.log(deviceSnapshot.key);
          deviceIdentifiers.push(deviceSnapshot.key);
        });

        // GET GAME WINNER AND GAME RESPONSE

        admin.database().ref(`games/${gameID}/assassinWon`).once('value', function(assassinWonSnapshot) {
          let result = assassinWonSnapshot.val();
          let winnerText = "";

          if (result) {
            winnerText = "Assassin"
          } else {
            winnerText = "Citizens"
          }

          const payload = {
            data: {
              type: "game_end_message",
              winner: winnerText,
              message: resultText
            }
          }; // payload

          return admin.messaging().sendToDevice(deviceIdentifiers, payload).then(response => {
            // For each message check if there was an error.
            const tokensToRemove = [];
            response.results.forEach((result, index) => {
              const error = result.error;
              if (error) {
                console.error('Failure sending notification to', playerDevice, error);
                // Cleanup the tokens who are not registered anymore.
                if (error.code === 'messaging/invalid-registration-token' ||
                  error.code === 'messaging/registration-token-not-registered') {
                  // tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                }
              }
            }); // forEach
          }); // admin.messaging()

        });

      }); // ref.once()
    }); // players.forEach
  }); // resolve gamePlayersPromise
}); // export endGame fxn

/*
 * Intermediate function that handles distributing the game end message to all players of a
 * particular game.
 *
 * TODO: Refactor this to be applicable to any message (game start, player joined)
 */
function sendGameEndNotificationToPlayer(playerName, gameName, resultText) {
  admin.database().ref(`users/${playerName}/device`).once('value', function(playerDeviceSnapshot) {
    console.log(playerDeviceSnapshot.val());

    // return Promise.resolve(playerDeviceSnapshot.val()).then(function(playerDeviceResult) {
    //   const playerDevice = playerDeviceResult;
    //   console.log("playerDeviceResult: " + playerDeviceResult);
    //   console.log("playerDeviceResult[keys]: " + Object.keys(playerDeviceResult));
    //
    //   const payload = {
    //     data: {
    //       type: "game_end_message",
    //       player: playerName,
    //     }
    //   };

      // return admin.messaging().sendToDevice(playerDevice, payload).then(response => {
      //   // For each message check if there was an error.
      //   const tokensToRemove = [];
      //   response.results.forEach((result, index) => {
      //     const error = result.error;
      //     if (error) {
      //       console.error('Failure sending notification to', playerDevice, error);
      //       // Cleanup the tokens who are not registered anymore.
      //       if (error.code === 'messaging/invalid-registration-token' ||
      //         error.code === 'messaging/registration-token-not-registered') {
      //         // tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
      //       }
      //     }
      //   }); // forEach
      // }); // admin.messaging()
    // }); // Promise.resolve()...

  });
}

/**
 * This function is sent to a game admin when a particular user is not logged
 * in and hence cannot respond to a game invitation.
 */
// exports.sendPlayerNotLoggedInResponse = functions.https.onRequest((request, response) => {
//   // Logic for sendPlayerNotLoggedInResponse goes here
// });
