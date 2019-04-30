package codes.fepi;

import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SecurityHandler {
	private final String secret;

	public SecurityHandler() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("").resolve("secret.secret").toAbsolutePath());
		secret = new String(bytes).trim();
	}

	public void secure() {
		Spark.before(this::filterRequest);
	}

	private void filterRequest(Request req, Response res) {
		String suppliedSecret = req.queryParams("secret");
		if(suppliedSecret == null || !suppliedSecret.equals(secret)){
			Spark.halt(401, "Wrong secret");
		}
	}
}
