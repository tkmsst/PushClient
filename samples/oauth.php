<?php
	$keyFile = 'service-account-file.json';
	$duration = 3600;
	$tokenFile = '/tmp/access_token.dat';

	// Base64URL encode
	function base64url_encode($data) {
		return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
	}

	// Read credentials from JSON file
	$credentials = json_decode(file_get_contents($keyFile), true);
	if (empty($credentials))
		exit('Failed to parse service account credentials.');

	// Create a JWT
	$header = base64url_encode('{"alg":"RS256","typ":"JWT"}');
	$url = 'https://oauth2.googleapis.com/token';
	$now = time();
	$claimSet = base64url_encode(json_encode(array(
		'iss' => $credentials['client_email'],
		'scope' => 'https://www.googleapis.com/auth/cloud-platform',
		'aud' => $url,
		'exp' => $now + $duration,
		'iat' => $now
	)));
	$signatureInput = $header . '.' . $claimSet;

	// Sign the JWT with the private key
	$signature = '';
	if (!openssl_sign($signatureInput, $signature, $credentials['private_key'], 'SHA256'))
		exit('Failed to sign the JWT.');
	$signedJwt = $signatureInput . '.' . base64url_encode($signature);

	// Send a request to obtain a token
	$data = http_build_query(array(
		'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
		'assertion' => $signedJwt
	));
	$header = array(
		"Content-Type: application/x-www-form-urlencoded",
		"Content-Length: " . strlen($data)
	);
	$context = array(
		'http' => array(
			'method'  => 'POST',
			'header'  => implode("\r\n", $header),
			'content' => $data
		)
	);

	$response = json_decode(file_get_contents($url, false, stream_context_create($context)), true);
	if (empty($response['access_token']))
		exit('Failed to obtain the access token.');

	// Output the token
	$fp = fopen($tokenFile, 'w');
	if (!$fp)
		exit('Filed to open' . $tokenFile . '.');
	fwrite($fp, $response['access_token']);
	fclose($fp);

	echo 'Completed.';
	echo PHP_EOL;
?>
