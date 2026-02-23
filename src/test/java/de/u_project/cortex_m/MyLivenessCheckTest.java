package de.u_project.cortex_m;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class MyLivenessCheckTest
{
	@Test
	void testLivenessUp()
	{
		given()
			.when()
			.get("/q/health/live")
			.then()
			.statusCode(200)
			.body("status", is("UP"))
			.body("checks.find { it.name == 'alive' }.status", is("UP"));
	}
}
