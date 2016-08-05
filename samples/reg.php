<?php
	$file_path = "/usr/local/sbin/push.dat";

	if (isset($_POST['register']))
		$token = $_POST['register'];
	elseif (isset($_POST['unregister']))
		$token = $_POST['unregister'];
	else {
		http_response_code(403);
		exit;
	}

	if (strlen($token) < 10) {
		exit('Invalid token.');
	}

	$fp = fopen($file_path, 'c+');
	if (!$fp)
        exit('File access failed.');
	if (!flock($fp, LOCK_EX)) {
		fclose($fp);
		exit('File lock failed.');
	}

	$registered = FALSE;
	$buf = "";
	while ($line = fgets($fp)) {
		if (isset($_POST['register'])) {
			if ($line == $token . "\n") {
				flock($fp, LOCK_UN);
				fclose($fp);
				exit('The token is already registered.');
			}
		} elseif ($line == $token . "\n")
			$registered = TRUE;
		else
			$buf .= $line;
	}
	
	if (isset($_POST['register'])) {
		if (fwrite($fp, $token . "\n"))
			echo 'The token is successfully registered.';
		else
			echo 'Failed to register the token.';
		fflush($fp);
	} elseif ($registered) {
		rewind($fp);
		if (fwrite($fp, $buf))
			echo 'The token is successfully unregistered.';
		else
			echo 'Failed to unregister the token.';
		fflush($fp);
		ftruncate($fp, ftell($fp));
	} else
		echo 'The token is not registered.';
	
	flock($fp, LOCK_UN);
	fclose($fp);
?>
