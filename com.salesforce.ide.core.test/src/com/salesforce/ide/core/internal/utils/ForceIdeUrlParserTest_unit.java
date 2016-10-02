package com.salesforce.ide.core.internal.utils;

import org.junit.Test;
import com.salesforce.ide.core.internal.utils.ForceIdeUrlParser;

import junit.framework.TestCase;

public class ForceIdeUrlParserTest_unit extends TestCase {

	@Test
	public void testUnwihtPackage2() {
		ForceIdeUrlParser parserUnPwSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&un=me@you.com&package=foo&pw=123456");
		assertEquals("me@you.com", parserUnPwSecure.getUsernamne());
		assertEquals("123456", parserUnPwSecure.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwSecure.getOrgName());
		assertEquals("myMachine:6109", parserUnPwSecure.getAddress());
		assertEquals("foo", parserUnPwSecure.getPackageName());
		assertEquals(true, parserUnPwSecure.isValid());
	}

	@Test
	public void testUnWithPackage() {
		ForceIdeUrlParser parserUnPwSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=1&un=me@you.com&pw=123456&package=foo");
		assertEquals("me@you.com", parserUnPwSecure.getUsernamne());
		assertEquals("123456", parserUnPwSecure.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwSecure.getOrgName());
		assertEquals("myMachine:6109", parserUnPwSecure.getAddress());
		assertEquals("foo", parserUnPwSecure.getPackageName());
		assertEquals(true, parserUnPwSecure.isValid());
	}

	@Test
	public void testSessSetSecure() {
		ForceIdeUrlParser parserSessSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=1&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessSecure.getOrgName());
		assertEquals("myMachine:6109", parserSessSecure.getAddress());
		assertEquals(true, parserSessSecure.getIsSecure());
		assertEquals("111222333444555666777", parserSessSecure.getSessionId());
		assertEquals(true, parserSessSecure.isValid());
	}

	@Test
	public void testDefaultSecure() {
		ForceIdeUrlParser parserSessSecure = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessSecure.getOrgName());
		assertEquals("myMachine:6109", parserSessSecure.getAddress());
		assertEquals(true, parserSessSecure.getIsSecure());
		assertEquals("111222333444555666777", parserSessSecure.getSessionId());
		assertEquals("", parserSessSecure.getOrgId());
		assertEquals(true, parserSessSecure.isValid());
	}

	@Test
	public void testSessNoSessId() {
		ForceIdeUrlParser parserSessIdInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0");
		assertEquals("subscriberDebugProject", parserSessIdInvalid.getOrgName());
		assertEquals("myMachine:6109", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("", parserSessIdInvalid.getSessionId());
		assertEquals(false, parserSessIdInvalid.isValid());
	}

	@Test
	public void testSessNoUrl() {
		ForceIdeUrlParser parserSessIdInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?&secure=0&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessIdInvalid.getOrgName());
		assertEquals("", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdInvalid.getSessionId());
		assertEquals(false, parserSessIdInvalid.isValid());
	}

	@Test
	public void testSessNoOrgName() {
		ForceIdeUrlParser parserSessIdInvalid = new ForceIdeUrlParser("forceide:///?url=myMachine%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777");
		assertEquals("", parserSessIdInvalid.getOrgName());
		assertEquals("myMachine:6109", parserSessIdInvalid.getAddress());
		assertEquals(false, parserSessIdInvalid.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdInvalid.getSessionId());
		assertEquals(false, parserSessIdInvalid.isValid());
	}

	@Test
	public void testUnNoCommand() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&secure=0&un=me@you.com&pw=123456");
		assertEquals("me@you.com", parserUnPwInvalid.getUsernamne());
		assertEquals("123456", parserUnPwInvalid.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("myMachine:6109", parserUnPwInvalid.getAddress());
		assertEquals("", parserUnPwInvalid.getCommand());
		assertEquals(false, parserUnPwInvalid.isValid());
	}

	@Test
	public void testUnNoUrl() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals("me@you.com", parserUnPwInvalid.getUsernamne());
		assertEquals("123456", parserUnPwInvalid.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("", parserUnPwInvalid.getAddress());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals(false, parserUnPwInvalid.isValid());
	}

	@Test
	public void testUnNoOrgName() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide:///?url=myMachine%3A6109&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals("me@you.com", parserUnPwInvalid.getUsernamne());
		assertEquals("123456", parserUnPwInvalid.getPassword());
		assertEquals("", parserUnPwInvalid.getOrgName());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals("myMachine:6109", parserUnPwInvalid.getAddress());
		assertEquals(false, parserUnPwInvalid.isValid());
	}

	@Test
	public void testNoPassword() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0&un=me@you.com");
		assertEquals("me@you.com", parserUnPwInvalid.getUsernamne());
		assertEquals("", parserUnPwInvalid.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("myMachine:6109", parserUnPwInvalid.getAddress());
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals(false, parserUnPwInvalid.isValid());
	}

	@Test
	public void testNoUsername() {
		ForceIdeUrlParser parserUnPwInvalid = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0&pw=123456");
		assertEquals("createproject", parserUnPwInvalid.getCommand());
		assertEquals("123456", parserUnPwInvalid.getPassword());
		assertEquals("subscriberDebugProject", parserUnPwInvalid.getOrgName());
		assertEquals("myMachine:6109", parserUnPwInvalid.getAddress());
		assertEquals(false, parserUnPwInvalid.isValid());
	}

	@Test
	public void testUpperText() {
		ForceIdeUrlParser parserSessIdUpper = new ForceIdeUrlParser("FORCEIDE://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777");
		assertEquals("subscriberDebugProject", parserSessIdUpper.getOrgName());
		assertEquals("myMachine:6109", parserSessIdUpper.getAddress());
		assertEquals(false, parserSessIdUpper.getIsSecure());
		assertEquals("111222333444555666777", parserSessIdUpper.getSessionId());
		assertEquals("createproject", parserSessIdUpper.getCommand());
		assertEquals(true, parserSessIdUpper.isValid());
	}

	@Test
	public void testUnSuccess() {
		ForceIdeUrlParser parserUnPw = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0&un=me@you.com&pw=123456");
		assertEquals("me@you.com", parserUnPw.getUsernamne());
		assertEquals("123456", parserUnPw.getPassword());
		assertEquals("subscriberDebugProject", parserUnPw.getOrgName());
		assertEquals("myMachine:6109", parserUnPw.getAddress());
		assertEquals("createproject", parserUnPw.getCommand());
		assertEquals(true, parserUnPw.isValid());
	}

	@Test
	public void testSessionSuccess() {
		ForceIdeUrlParser parserSessId = new ForceIdeUrlParser("forceide://subscriberDebugProject/?url=myMachine%3A6109&cmd=createproject&secure=0&sessionId=1112223334!44555666777");
		assertEquals("subscriberDebugProject", parserSessId.getOrgName());
		assertEquals("myMachine:6109", parserSessId.getAddress());
		assertEquals(false, parserSessId.getIsSecure());
		assertEquals("1112223334!44555666777", parserSessId.getSessionId());
		assertEquals("1112223334", parserSessId.getOrgId());
		assertEquals("createproject", parserSessId.getCommand());
		assertEquals(parserSessId.isValid(), true);
	}
}
