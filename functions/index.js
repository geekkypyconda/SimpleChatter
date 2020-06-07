'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Notification/{receiver_user_id}/{notification_id}')
.onWrite((data,context) =>
{
	const receiver_user_id = context.params.receiver_user_id;
	const notification_id = context.params.notification_id;

	console.log('We have notification to send to :',receiver_user_id);

	if(!data.after.val())
	{
		console.log('A notification has been deleted :',notification_id);
		return null;
	}


	const sender_user_id = admin.database().ref(`/Notifications/${receiver_user_id}/${notification_id}`).once('value');

	return sender_user_id.then(fromUserResult =>
	{
		const from_sender_user_id = fromUserResult.val().from;
		console.log('You have a Notification from :', sender_user_id);
		const userQuery = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');

		return userQuery.then(userResult =>
		{
			const senderUserName = userResult.val();
		});

		const deviceToken = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');
	
		return deviceToken.then(result =>
		{
			const token_id = result.val();
			const payload =
			{
				notification:
				{
					from_sender_user_id : from_sender_user_id,
					title: "Simple Chatter",
					body: `You have a new Chat Request from ${senderUserName}`,
					icon: "default"
				}
			};

			return admin.messaging().sendToDevice(token_id,payload)
			.then(response =>
			{
				console.log('This Is A Notification Feature');
			});
		});
	});
});


	