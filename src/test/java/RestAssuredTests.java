import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.Random;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class RestAssuredTests {
    //http://restful-booker.herokuapp.com/apidoc/index.html

    public static String TOKEN_VALUE;
    public static final String TOKEN = "token";

    @BeforeMethod
    public void setUp(){
        RestAssured.baseURI="https://restful-booker.herokuapp.com";
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .build();

        JSONObject body = new JSONObject();
        body.put("username", "admin");
        body.put("password", "password123");

        Response response = RestAssured.given()
                .body(body.toString())
                .post("/auth");
        TOKEN_VALUE = response.then().extract().jsonPath().get(TOKEN);
    }

    @Test(description = "This test checks positive case of getting all booking IDs")
    public void getAllBookingIdsTest(){
        Response allBookingIds = RestAssured.given().log().all().get("/booking");
        allBookingIds.then().statusCode(200);
        allBookingIds.prettyPrint();
//        allBookingIds.jsonPath().get("bookingid.findAll{it>100}.bookingid");
        allBookingIds.then().assertThat().body(matchesJsonSchemaInClasspath("jsonAllBookingIdsSchema.json"));
    }

    @Test(description = "This test checks positive case of getting a booking by ID")
    public void getBookingByIdTest(){
        Response bookingId = RestAssured.given().log().all().get("/booking/{id}",1);
        bookingId.prettyPrint();
//        bookingId.then().body("firstname", equalTo("Sally"));
        bookingId.then().statusCode(200);
        bookingId.then().body("bookingdates.checkin", greaterThanOrEqualTo("2014-01-01"));
        bookingId.jsonPath().get("totalprice");
    }

    @Test(description = "This test checks positive case of creation a booking")
    public void createNewBookingTest(){
        String firstNameExpected = "TestName";
        Integer totalPriceExpected = 1000;
        Boolean depositPaidExpected = false;

        CreateBookingBody body = new CreateBookingBody().builder()
                .firstname("TestName")
                .lastname("TestLastName")
                .totalprice(1000)
                .depositpaid(false)
                .bookingdates(new BookingDates("2023-07-01","2023-07-05"))
                .additionalneeds("TestNeeds")
                .build();

        Response newBooking = RestAssured.given()
                .body(body)
                .post("/booking");
        newBooking.prettyPrint();
        newBooking.as(ResponseBooking.class);
        String firstNameResponse = newBooking.as(ResponseBooking.class).getBooking().getFirstname();
        Integer totalPriceResponse = newBooking.as(ResponseBooking.class).getBooking().getTotalprice();
        Boolean depositPaidResponse = newBooking.as(ResponseBooking.class).getBooking().getDepositpaid();
        newBooking.then().statusCode(200);
        newBooking.then().body("bookingid", notNullValue());
        Assert.assertEquals(firstNameResponse, firstNameExpected, "The First name is wrong");
        Assert.assertEquals(totalPriceResponse, totalPriceExpected, "The Total price is wrong");
        Assert.assertEquals(depositPaidResponse, depositPaidExpected, "The Deposit paid is wrong");
    }

    @Test(description = "This test checks positive case of partial booking update")
    public void partialUpdateBookingTest(){

        JSONObject body = new JSONObject();
        body.put("totalprice", 500);

        Response updatedBooking = RestAssured.given()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .cookie(TOKEN, TOKEN_VALUE)
                .body(body.toString())
                .patch("/booking/{id}", 919);
        updatedBooking.prettyPrint();
        updatedBooking.then().statusCode(200);
    }

    @Test(description = "This test checks positive case of updating a booking")
    public void updateBookingTest(){
        String lastNameExpected = "TestLastNameModified";
        CreateBookingBody body = new CreateBookingBody().builder()
                .firstname("TestNameModified")
                .lastname("TestLastNameModified")
                .totalprice(1000)
                .depositpaid(true)
                .bookingdates(new BookingDates("2023-07-01","2023-07-05"))
                .additionalneeds("TestNeedsModified")
                .build();

        Response updatedBooking = RestAssured.given()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .cookie(TOKEN, TOKEN_VALUE)
                .body(body)
                .put("/booking/{id}", 1895);
        updatedBooking.prettyPrint();
        String lastNameResponse = updatedBooking.as(CreateBookingBody.class).getLastname();
        updatedBooking.then().statusCode(200);
        Assert.assertEquals(lastNameResponse, lastNameExpected, "The Last name is wrong");
    }

    @Test(description = "This test checks positive case of removal a booking")
    public void deleteBookingTest(){
        Random rand = new Random();
        int upperbound = 2000;
        int int_random = rand.nextInt(upperbound);

        Response deleteBooking = RestAssured.given()
                .cookie(TOKEN, TOKEN_VALUE)
                .delete("/booking/{id}",int_random);
        deleteBooking.prettyPrint();
        deleteBooking.then().statusCode(201);
    }
}
