#!/bin/sh

CREDENTIALS='service-account-file.json'
DURATION=3600
TOKEN_FILE='/tmp/access_token.dat'

HEADER='{"alg":"RS256","typ":"JWT"}'
EMAIL=`grep -Po '"client_email" *: *"\K.*?(?=")' "${CREDENTIALS}"`
URL=https://oauth2.googleapis.com/token
TIME=`date +%s`
EXPIRY=`expr ${TIME} + ${DURATION}`
CLAIMSET='{"iss":"'${EMAIL}'","scope":"https://www.googleapis.com/auth/firebase.messaging","aud":"'${URL}'","exp":'${EXPIRY}',"iat":'${TIME}'}'
B64HEADER=`echo -n ${HEADER} | basenc -w0 --base64url`
B64CLAIMSET=`echo -n ${CLAIMSET} | basenc -w0 --base64url`

PRIVATE_KEY=`grep -Po '"private_key" *: *"\K.*?(?=")' "${CREDENTIALS}"`
KEY_FILE='/tmp/private_key.pem'
echo "${PRIVATE_KEY}" > "${KEY_FILE}"
SIGNATURE=`echo -n ${B64HEADER}.${B64CLAIMSET} | openssl dgst -sha256 -sign "${KEY_FILE}" | basenc -w0 --base64url`
rm "${KEY_FILE}"
JWT=${B64HEADER}.${B64CLAIMSET}.${SIGNATURE}

RESPONSE=`curl -d "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=${JWT}" ${URL}`
echo ${RESPONSE} | grep -Po '"access_token" *: *"\K.*?(?=")' > "${TOKEN_FILE}"
