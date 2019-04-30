package codes.fepi;

import spark.Spark;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		Spark.port(6006);
		RequestHandler requestHandler = new RequestHandler();
		SecurityHandler securityHandler = new SecurityHandler();
		securityHandler.secure();
		requestHandler.setup();
	}
}
