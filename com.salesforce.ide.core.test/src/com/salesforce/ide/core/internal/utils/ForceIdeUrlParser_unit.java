package com.salesforce.ide.core.internal.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import com.salesforce.ide.core.internal.utils.ForceIdeUrlParser;

public class ForceIdeUrlParser_unit {

	@Test
	public void testUnwihtPackage2() {
		ForceIdeUrlParser parserUnPwSecure;
		parserUnPwSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&un=me@you.com&package=foo&pw=123456");
		assertEquals( parserUnPwSecure.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwSecure.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPwSecure.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPwSecure.getAddress());
		assertEquals("foo", parserUnPwSecure.getPackageName());
		assertEquals(parserUnPwSecure.isValid(), true);
	}

	@Test
	public void testUnWithPackage() {
		ForceIdeUrlParser parserUnPwSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=1&un=me@you.com&pw=123456&package=foo");
		assertEquals( parserUnPwSecure.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwSecure.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPwSecure.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPwSecure.getAddress());
		assertEquals("foo", parserUnPwSecure.getPackageName());
		assertEquals(parserUnPwSecure.isValid(), true);
	}

	@Test
	public void testSessSetSecure() {
		ForceIdeUrlParser parserSessSecure;
		parserSessSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=1&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessSecure.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessSecure.getAddress());
		assertEquals(true, parserSessSecure.getIsSecure());
		assertEquals("111222333444555666777", parserSessSecure.getSessionId());
		assertEquals(parserSessSecure.isValid(), true);
	}

	@Test
	public void testDefaultSecure() {
		ForceIdeUrlParser parserSessSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessSecure.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessSecure.getAddress());
		assertEquals(true, parserSessSecure.getIsSecure());
		assertEquals("111222333444555666777", parserSessSecure.getSessionId());
		assertEquals(parserSessSecure.isValid(), true);
	}

	@Test
	public void testSessNoSessId() {
		ForceIdeUrlParser parserSessIdInvalid;
		parserSessIdInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0");
		assertEquals("subscriberDebugProject", parserSessIdInvalid.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("", parserSessIdInvalid.getSessionId());
		assertEquals(parserSessIdInvalid.isValid(), false);
	}

	@Test
	public void testSessNoUrl() {
		ForceIdeUrlParser parserSessIdInvalid;
		parserSessIdInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?&secure=0&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessIdInvalid.getOrgName());
		assertEquals("", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdInvalid.getSessionId());
		assertEquals(parserSessIdInvalid.isValid(), false);
	}

	@Test
	public void testSessNoOrgName() {
		ForceIdeUrlParser parserSessIdInvalid = new ForceIdeUrlParser("forceide:///?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777");
		assertEquals("", parserSessIdInvalid.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdInvalid.getSessionId());
		assertEquals(parserSessIdInvalid.isValid(), false);
	}

	@Test
	public void testUnNoCommand() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&secure=0&un=me@you.com&pw=123456");
		assertEquals( parserUnPwInvalid.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwInvalid.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPwInvalid.getAddress());
		assertEquals("", parserUnPwInvalid.getCommand());
		assertEquals(parserUnPwInvalid.isValid(), false);
	}

	@Test
	public void testUnNoUrl() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals( parserUnPwInvalid.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwInvalid.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("", parserUnPwInvalid.getAddress());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals(parserUnPwInvalid.isValid(), false);
	}

	@Test
	public void testUnNoOrgName() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide:///?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals( parserUnPwInvalid.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwInvalid.getPassword(), "123456");
		assertEquals("", parserUnPwInvalid.getOrgName());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals("dbaker-wsm:6109", parserUnPwInvalid.getAddress());
		assertEquals(parserUnPwInvalid.isValid(), false);
	}

	@Test
	public void testNoPassword() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&un=me@you.com");
		assertEquals( parserUnPwInvalid.getUsernamne(), "me@you.com");
		assertEquals(parserUnPwInvalid.getPassword(), "");
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPwInvalid.getAddress());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals(parserUnPwInvalid.isValid(), false);
	}

	@Test
	public void testNoUsername() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&pw=123456");
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals(parserUnPwInvalid.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPwInvalid.getAddress());
		assertEquals(parserUnPwInvalid.isValid(), false);
	}

	@Test
	public void testUpperText() {
		ForceIdeUrlParser parserSessIdUpper = new ForceIdeUrlParser("FORCEIDE://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessIdUpper.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessIdUpper.getAddress());
		assertEquals(false, parserSessIdUpper.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdUpper.getSessionId());
		assertEquals("createproject", parserSessIdUpper.getCommand());
		assertEquals(parserSessIdUpper.isValid(), true);
	}

	public void testUnSuccess() {
		ForceIdeUrlParser parserUnPw = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals( parserUnPw.getUsernamne(), "me@you.com");
		assertEquals(parserUnPw.getPassword(), "123456");
		assertEquals("subscriberDebugProject", parserUnPw.getOrgName());
		assertEquals("dbaker-wsm:6109", parserUnPw.getAddress());
		assertEquals("createproject", parserUnPw.getCommand());
		assertEquals(parserUnPw.isValid(), true);
	}

	@Test
	public void testSessionSuccess() {
		ForceIdeUrlParser parserSessId = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=dbaker-wsm%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessId.getOrgName());
		assertEquals("dbaker-wsm:6109", parserSessId.getAddress());
		assertEquals(false, parserSessId.getIsSecure());
		assertEquals("111222333444555666777", parserSessId.getSessionId());
		assertEquals("createproject", parserSessId.getCommand());
		assertEquals(parserSessId.isValid(), true);
	}
}
