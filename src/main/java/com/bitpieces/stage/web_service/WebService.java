package com.bitpieces.stage.web_service;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;

import com.bitpieces.shared.DataSources;
import com.bitpieces.shared.tools.CoinbaseTools;
import com.bitpieces.shared.tools.DBActions;
import com.bitpieces.shared.tools.Tools;
import com.bitpieces.shared.tools.UID;
import com.bitpieces.shared.tools.UnitConverter;
import com.bitpieces.shared.tools.WebCommon;
import com.bitpieces.shared.tools.WebTools;
import com.coinbase.api.Coinbase;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

// 
// java -cp bitpieces_practice-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.bitpieces.dev.web_service.WebService
public class WebService {
	public static final String COOKIE_PATH = "prod";

	// Use an expiring map to store the authenticated sessions
	private static Cache<String, UID> SESSION_TO_USER_MAP = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterAccess(WebCommon.COOKIE_EXPIRE_SECONDS, TimeUnit.SECONDS) // expire it after its been accessed
			.build();

	public static void main(String[] args) {

		// Set up coinbase for operations
		Coinbase cb = CoinbaseTools.setupCoinbase(DataSources.COINBASE_PROP);

		// Load the correct db connection
		Properties prop = Tools.loadProperties(DataSources.STAGE_DB_PROP);

		// Load the correct session cache
		SESSION_TO_USER_MAP.putAll(Tools.readObjectFromFile(DataSources.STAGE_SESSION_FILE));

		// Set the correct port
		setPort(DataSources.STAGE_WEB_PORT);

		// Get an instance of the currency/precision converter
		UnitConverter sf = new UnitConverter();

		// Setup all the common gets
		WebCommon.commonGets(SESSION_TO_USER_MAP, prop, sf, DataSources.STAGE_SESSION_FILE, COOKIE_PATH);

		// Setup all the common posts
		WebCommon.commonPosts(SESSION_TO_USER_MAP, prop, sf, DataSources.STAGE_SESSION_FILE, COOKIE_PATH);



		
		post("/registeruser", (req, res) -> {
			WebCommon.allowResponseHeaders(req, res);
			dbInit(prop);
			try {
				// Verify the recaptcha
				WebTools.recaptcha(req.url(), req.body());

				// Create the user
				UID uid = DBActions.createUserRealFromAjax(req.body());

				dbClose();

				// Its null if it couldn't create the user, usually cause of constraints
				if (uid != null) {
					String message = WebCommon.verifyLoginAndSetCookies(uid, req, res, SESSION_TO_USER_MAP, 
							DataSources.STAGE_SESSION_FILE, COOKIE_PATH);

					return "user registered";
				} else {

					res.status(666);
					return "User already exists";
				}

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/registercreator", (req, res) -> {
			WebCommon.allowResponseHeaders(req, res);
			dbInit(prop);
			try {
				// Verify the recaptcha
				WebTools.recaptcha(req.url(), req.body());

				// Create the user
				UID uid = DBActions.createCreatorRealFromAjax(cb, req.body());

				dbClose();

				// Its null if it couldn't create the user, usually cause of constraints
				if (uid != null) {
					String message = WebCommon.verifyLoginAndSetCookies(uid, req, res, SESSION_TO_USER_MAP, 
							DataSources.STAGE_SESSION_FILE, COOKIE_PATH);

					return "creator registered";
				} else {

					res.status(666);
					return "Creator already exists";
				}
			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/placebid", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);

				uid.verifyUser();

				message = WebTools.placeBid(uid, req.body(), sf);

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		post("/placeask", (req, res) -> {
			WebCommon.allowResponseHeaders(req, res);

			dbInit(prop);

			// get the creator id from the token
			UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
			String message = null;
			try {
				message = WebTools.placeAsk(uid, req.body(), sf);
			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

			dbClose();


			return message;

		});



		post("/placebuy", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyUser();

				message = WebTools.placeBuy(uid, req.body());

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		post("/issue_pieces", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyCreator();

				message = WebTools.issuePieces(uid, req.body(), sf);

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		post("/new_reward", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyCreator();

				message = WebTools.newReward(uid, req.body(), sf);

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		post("/raise_funds", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyCreator();

				message = WebTools.raiseFunds(uid, req.body(), sf);

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		post("/delete_bid_ask", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyUser();

				message = WebTools.deleteBidAsk(uid, req.body());

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});

		get("/deposit_button", (req, res) -> {
			String code = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				dbInit(prop);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyUser();

				code = CoinbaseTools.fetchOrCreateDepositButton(cb, uid);

				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

			return code;

		});

		post("/:user_id/coinbase_deposit_callback", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				System.out.println(req.body());
				dbInit(prop);
				String userId = req.params(":user_id");


				WebTools.makeDepositFromCoinbaseCallback(userId, req.body());
				dbClose();
			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
			return message;

		});



		post("/user_withdraw", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				UID uid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				uid.verifyUser();
				dbInit(prop);

				message = WebTools.makeUserWithdrawal(cb, uid, req.body(), sf);
				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}
			return message;

		});

		post("/creator_withdraw", (req, res) -> {
			String message = null;
			try {
				WebCommon.allowResponseHeaders(req, res);
				UID cid = WebCommon.getUserFromCookie(req, SESSION_TO_USER_MAP, COOKIE_PATH);
				cid.verifyCreator();

				dbInit(prop);

				message = WebTools.makeCreatorWithdrawal(cb, cid, req.body(), sf);
				dbClose();

			} catch (NoSuchElementException e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}
			return message;

		});

	}



	private static final void dbInit(Properties prop) {
		try {
			Base.open("com.mysql.jdbc.Driver", 
					prop.getProperty("dburl"), 
					prop.getProperty("dbuser"), 
					prop.getProperty("dbpassword"));
		} catch (DBException e) {
			dbClose();
			dbInit(prop);
		}


	}

	private static final void dbClose() {
		Base.close();
	}






}
