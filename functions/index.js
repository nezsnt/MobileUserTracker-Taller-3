const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

exports.notifyAvailability = functions.database
    .ref('/users/{triggerUserId}/available')
    .onUpdate(async (change, context) => {

        const wasAvailable = change.before.val();
        const isAvailableNow = change.after.val();

        if (isAvailableNow === true && wasAvailable !== true) {
            const triggerUserId = context.params.triggerUserId;

            const senderSnapshot = await admin.database().ref(`/users/${triggerUserId}`).once('value');
            const senderData = senderSnapshot.val();

            const allUsersSnapshot = await admin.database().ref('/users').once('value');
            const allUsers = allUsersSnapshot.val();

            const tokensToSend = [];

            for (const uid in allUsers) {
                if (uid !== triggerUserId) {
                    const userToken = allUsers[uid].fcmToken;
                    if (userToken) {
                        tokensToSend.push(userToken);
                    }
                }
            }
            if (tokensToSend.length === 0) {
                console.log("No available users");
                return null;
            }
            const message = {
                notification: {
                    title: "New User Available",
                    body: `${senderData.name} ${senderData.lastname} just connected.`
                },
                data: {
                    trackUserId: triggerUserId
                },
                tokens: tokensToSend
            };
            try {
                const response = await admin.messaging().sendEachForMulticast(message);
                return null;
            } catch (error) {
                console.error("Failed Sending Notifications", error);
                return null;
            }
        }
        return null;
    });