/* eslint-disable */
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const ADMIN_UID = '4FRGkuFpHhVVaKSXitxm407myR92';

exports.createUser = functions.https.onCall((data, context) => {
    if (context.auth.uid !== ADMIN_UID) {
        return { error: "Only the admin can perform this operation." };
    }

    const email = data.email;
    const password = data.password;

    let uid;

    return admin.auth().createUser({
        email: email,
        password: password
    })
    .then(userRecord => {
        uid = userRecord.uid;
        const userData = {
            firstName: data.firstName,
            lastName: data.lastName,
            dateOfBirth: data.dateOfBirth,
            favoriteTeam: data.favoriteTeam,
            securityQuestion: data.securityQuestion,
            securityAnswer: data.securityAnswer
        };
        return admin.firestore().collection('users').doc(uid).set(userData);
    })
    .then(() => {
        console.log(`Successfully created user ${email}`);
        return { message: `Successfully created user ${email}` };
    })
    .catch(error => {
        console.error(`Error creating user: ${error.message}`);
        return { error: error.message };
    });
});

exports.deleteUser = functions.https.onCall((data, context) => {
    if (context.auth.uid !== ADMIN_UID) {
        return { error: "Only the admin can perform this operation." };
    }

    const email = data.email;

    let uid;
    return admin.auth().getUserByEmail(email)
        .then(userRecord => {
            uid = userRecord.uid;
            return admin.auth().deleteUser(uid);
        })
        .then(() => {
            console.log(`Successfully deleted user ${email}`);
            return admin.firestore().collection('users').doc(uid).delete();
        })
        .then(() => {
            console.log(`Successfully deleted Firestore document for user ${email}`);
            return { message: `Successfully deleted user ${email}` };
        })
        .catch(error => {
            console.error(`Error deleting user: ${error.message}`);
            return { error: error.message };
        });
});
