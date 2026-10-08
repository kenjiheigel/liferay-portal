/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.processor;

import com.liferay.petra.string.StringBundler;
import com.liferay.source.formatter.SourceFormatterArgs;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Alejandro Tardín
 */
public class ReviewRulesSourceProcessorTest
	extends BaseSourceProcessorTestCase {

	@Test
	public void testAssertMessage() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"AssertMessage.testjava"
			).addExpectedMessage(
				"Do not pass a message to \"Assert.assertFalse\"", 17
			).addExpectedMessage(
				"Do not pass a message to \"Assert.assertTrue\"", 19
			));
	}

	@Test
	public void testCaseStatement() throws Exception {
		test(
			"CaseStatement.testsh",
			"Use an \"if\" statement instead of a \"case\" statement", 6);
	}

	@Test
	public void testConditionalAssignmentOrder() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"ConditionalAssignmentOrder.testjava"
			).addExpectedMessage(
				"The assignment of \"_beta\" should come before the " +
					"assignment of \"_delta\"",
				17
			));
	}

	@Test
	public void testConsecutiveMethodCallsOrder() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"ConsecutiveMethodCallsOrderTest.testjava"
			).addExpectedMessage(
				"The call to \"_addEntry\" should come before the call on " +
					"line \"17\" (sort by flattened text)",
				18
			).addExpectedMessage(
				"The method calling \"setModifiedTime\" should come before " +
					"the method calling \"setStatus\"",
				31
			));
	}

	@Test
	public void testIndexVariableName() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"IndexVariableName.testjava"
			).addExpectedMessage(
				"Rename \"spaceIndex\" to \"index\"", 14
			).addExpectedMessage(
				"Reassign \"firstSlashIndex\" instead of declaring " +
					"\"secondSlashIndex\", and name it \"index\"",
				34
			));
	}

	@Test
	public void testInlineComment() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"InlineComment.testjava"
			).addExpectedMessage(
				"Begin the comment with a capital letter", 15
			));
	}

	@Test
	public void testInlineCommentShell() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"InlineComment.testsh"
			).addExpectedMessage(
				"Begin the comment with a capital letter", 5
			).addExpectedMessage(
				"There should be an empty line after the comment", 8
			).addExpectedMessage(
				"There should be an empty line before the comment", 8
			));
	}

	@Test
	public void testInlineJSONSpacing() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"InlineJSONSpacing.testjava"
			).addExpectedMessage(
				"Put a space after each \":\" and \",\" in inline JSON", 10
			).addExpectedMessage(
				"Do not put a space just inside \"{\" or \"}\" in inline JSON",
				18
			));
	}

	@Test
	public void testIsEmptyUtil() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"IsEmptyUtil.testjava"
			).addExpectedMessage(
				"Use \"MapUtil.isEmpty(map)\" to simplify code", 25
			).addExpectedMessage(
				"Use \"SetUtil.isEmpty(set)\" to simplify code", 29
			).addExpectedMessage(
				"Use \"ArrayUtil.isNotEmpty(array)\" to simplify code", 33
			).addExpectedMessage(
				"Use \"ListUtil.isNotEmpty(list)\" to simplify code", 37
			));
	}

	@Test
	public void testIteratorLoop() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"IteratorLoop.testjava"
			).addExpectedMessage(
				"Use an enhanced \"for\" loop instead of iterating with " +
					"\"iterator\"",
				21
			));
	}

	@Test
	public void testLanguageTitleOrSentence() throws Exception {
		SourceProcessorTestParameters sourceProcessorTestParameters =
			SourceProcessorTestParameters.create(
				"titleorsentence/content/Language.testproperties");

		for (Object[] expectedMessage :
				new Object[][] {
					{"click-here", 2}, {"entries-with-errors", 4},
					{"prune-stale-entries", 7}, {"remove-every-stale-entry", 8},
					{"search-entries-from-x", 9}
				}) {

			sourceProcessorTestParameters.addExpectedMessage(
				StringBundler.concat(
					"The value of key \"", expectedMessage[0],
					"\" should be a title in APA title case or a complete ",
					"sentence ending in a period"),
				(Integer)expectedMessage[1]);
		}

		test(sourceProcessorTestParameters);
	}

	@Test
	public void testListName() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"ListName.testjava"
			).addExpectedMessage(
				"Rename \"namesList\" to \"names\"", 17
			).addExpectedMessage(
				"Rename \"_keysList\" to \"_keys\"", 23
			));
	}

	@Test
	public void testLongLiteralSuffix() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"LongLiteralSuffix.testjava"
			).addExpectedMessage(
				"Remove the \"L\" suffix from \"1000L\", the value fits in " +
					"an \"int\"",
				18
			).addExpectedMessage(
				"Remove the \"L\" suffix from \"5L\", the value fits in an " +
					"\"int\"",
				22
			).addExpectedMessage(
				"Remove the \"L\" suffix from \"3L\", the value fits in an " +
					"\"int\"",
				28
			).addExpectedMessage(
				"Remove the \"L\" suffix from \"300000L\", the value fits in " +
					"an \"int\"",
				33
			));
	}

	@Test
	public void testMarkdownParagraph() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"MarkdownParagraph.testmd"
			).addExpectedMessage(
				"Write each paragraph and list item on a single line", 6
			).addExpectedMessage(
				"Write each paragraph and list item on a single line", 9
			));
	}

	@Test
	public void testMessageConcatenation() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"MessageConcatenation.testjava"
			).addExpectedMessage(
				"Incorrect log message", 17
			).addExpectedMessage(
				"Incorrect exception message", 21
			));
	}

	@Test
	public void testOverrideAnnotation() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"OverrideAnnotation.testjava"
			).addExpectedMessage(
				"Add \"@Override\" to method \"compareTo\"", 18
			).addExpectedMessage(
				"Add \"@Override\" to method \"test\"", 29
			).addExpectedMessage(
				"Add \"@Override\" to method \"toString\"", 41
			));
	}

	@Test
	public void testPairedMethodName() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"PairedMethodName.testjava"
			).addExpectedMessage(
				"Rename \"getFoosCount\" to \"getFoosCountByGroupIds\" to " +
					"pair it with \"getFoosByGroupIds\"",
				19
			));
	}

	@Test
	public void testParameterOrder() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"ParameterOrder.testjava"
			).addExpectedMessage(
				"Parameter \"count\" should come before parameter \"name\"", 13
			).addExpectedMessage(
				"Parameter \"content\" should come before parameter \"path\"",
				24
			));
	}

	@Test
	public void testSHFailFast() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"cloud/SHFailFast.testsh"
			).addExpectedMessage(
				"Start the script with \"set -o errexit\", \"set -o " +
					"nounset\", and \"set -o pipefail\", in that order, " +
						"before the first command",
				1
			));
	}

	@Test
	public void testSHFailFastValid() throws Exception {
		test("cloud/SHFailFastValid.testsh");
	}

	@Test
	public void testSHFunctionOrder() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"SHFunctionOrder.testsh"
			).addExpectedMessage(
				"Function \"main\" should come before function \"_die\", " +
					"public functions come first, then private ones, each " +
						"sorted alphabetically",
				3
			));
	}

	@Test
	public void testSHIncrement() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"SHIncrement.testsh"
			).addExpectedMessage(
				"Use \"((++count))\" instead of \"((count++))\"", 8
			).addExpectedMessage(
				"Use \"((++count))\" instead of \"count=$((count + 1))\"", 11
			));
	}

	@Test
	public void testSHMessage() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"SHMessage.testsh"
			).addExpectedMessage(
				"Write the message as a phrase without a final period, or as " +
					"two or more sentences that each end with a period",
				4
			));
	}

	@Test
	public void testSentenceSpacing() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"SentenceSpacing.testjava"
			).addExpectedMessage(
				"Use a single space after a period", 17
			));
	}

	@Test
	public void testStringBuilder() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"StringBuilderUsage.testjava"
			).addExpectedMessage(
				"Use \"StringBundler\" instead of \"StringBuilder\"", 14
			).addExpectedMessage(
				"Use \"StringBundler\" instead of \"StringBuilder\"", 16
			));
	}

	@Test
	public void testTerraformTrailingNewline() throws Exception {
		test("TrailingNewline.testtf");
	}

	@Test
	public void testTransformUtil() throws Exception {
		test(
			SourceProcessorTestParameters.create(
				"TransformUtil.testjava"
			).addExpectedMessage(
				"Use \"TransformUtil.transform\" to build \"upperCaseNames\"",
				27
			).addExpectedMessage(
				"Use \"TransformUtil.transform\" to build \"trimmedNames\"", 37
			));
	}

	@Test
	public void testYMLHelmTestOrder() throws Exception {
		test("helm/tests/fixture_test.testyaml");
	}

	@Override
	protected SourceFormatterArgs getSourceFormatterArgs() {
		SourceFormatterArgs sourceFormatterArgs =
			super.getSourceFormatterArgs();

		sourceFormatterArgs.setCheckNames(
			Arrays.asList(
				"AssertMessageCheck", "ConditionalAssignmentOrderCheck",
				"ConsecutiveMethodCallsOrderCheck", "ExceptionMessageCheck",
				"IndexVariableNameCheck", "InlineCommentCheck",
				"InlineJSONSpacingCheck", "IsEmptyUtilCheck",
				"IteratorLoopCheck", "ListNameCheck", "LogMessageCheck",
				"LongLiteralSuffixCheck", "MarkdownParagraphCheck",
				"OverrideAnnotationCheck", "PairedMethodNameCheck",
				"ParameterOrderCheck", "PropertiesLanguageTitleOrSentenceCheck",
				"SentenceSpacingCheck", "SHCaseStatementCheck",
				"SHFailFastCheck", "SHFunctionOrderCheck", "SHIncrementCheck",
				"SHMessageCheck", "StringBuilderCheck", "TrailingNewlineCheck",
				"TransformUtilCheck", "YMLDefinitionOrderCheck"));

		List<String> sourceFormatterProperties =
			sourceFormatterArgs.getSourceFormatterProperties();

		sourceFormatterProperties.add(
			"checkstyle.ExceptionMessageCheck.checkConcatenatedMessages=true");
		sourceFormatterProperties.add(
			"checkstyle.LogMessageCheck.checkConcatenatedMessages=true");

		sourceFormatterArgs.setSourceFormatterProperties(
			sourceFormatterProperties);

		return sourceFormatterArgs;
	}

}