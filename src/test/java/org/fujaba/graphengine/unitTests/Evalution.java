package org.fujaba.graphengine.unitTests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.stateelimination.TTCStateCaseGraphLoader;
import org.junit.Test;

public class Evalution {
	
	
	public static final String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
	public static String positivePath = "src/main/resources/EvaluationFramework/testdata/acceptedWords/";
	public static String negativePath = "src/main/resources/EvaluationFramework/testdata/notAcceptedWords/";
	
//	@Test
	public void evaluateResults() {
		
		// the following already 'freezes' the CPU, because a long RegExp take its time...
//		String s1 = "s7s30s47s49s0s7s30s47s49s0s10s26s46s49s0s4s19s42s49s0s16s21s41s49s0s8s29s48s59";
//		String s2 = "(((((((((((((((s1)(s17))(s33))(s49))|((((s10)(s26))(s46))(s49)))|((((s13)(s23))(s38))(s49)))|((((s7)(s30))(s47))(s49)))|((((s16)(s21))(s41))(s49)))|((((s4)(s19))(s42))(s49)))(s0))*)*(((((s6)(s32))(s35))(s53))(s0))*)*)*(((((s11)(s28))(s43))(s56))(s0))*)*)*(((((((((((((s2)(s18))(s34))(s50))(s60))|(((((s5)(s31))(s37))(s55))(s60)))|(((((s15)(s22))(s39))(s51))(s60)))|(((((s3)(s20))(s40))(s52))(s60)))|(((((s14)(s24))(s36))(s54))(s60)))|(((((s12)(s27))(s44))(s57))(s60)))|(((((s9)(s25))(s45))(s58))(s60)))|(((((s8)(s29))(s48))(s59))(s60)))(s60)*)";
//		System.out.println(s1.matches(s2));
		
		for (String fileName: TestTTCStateCase.fileNamesTaskMain) {
			System.out.println("TTC2017 State Elimination: " + fileName + "...");
			long beginTime = System.nanoTime();
			Graph g = TTCStateCaseGraphLoader.load(taskMainPath + fileName); // get data
			Graph result = TestTTCStateCase.solveGraph(g); // solve problem
			long endTime = System.nanoTime();
			System.out.println("Done after " + ((endTime - beginTime) / 1e9) + " seconds.");
			String resultStringRaw = "";
			for (String s: result.getNodes().get(0).getEdges().keySet()) {
				resultStringRaw = s;
			}
			String resultString = resultStringRaw.replaceAll(Pattern.quote("(ε)"), "");
			resultString = resultString.replaceAll(Pattern.quote("ε"), "");
			System.out.println(resultString + "\n");

			String baseFileName = fileName.substring(0, fileName.length() - 4);
			
			System.out.println(testWords(resultString, positivePath + baseFileName + "-positive.data", true));
			System.out.println(testWords(resultString, negativePath + baseFileName + "-negative.data", false));
			
		}
	}

	private String formatRegex(String regex){
		regex = regex.replace('+', '|');	//java uses '|' as the or symbol
		regex = regex.replaceAll("\\[.*?\\]", "");	//remove probability
		regex = regex.replaceAll(":", "");	//':' is concatenation
		return regex;
	}
	
	private String testWords(String regex, String acceptedWordsFile, boolean accept){
		int totalWords = 0;
		int passed = 0;
		String word = "";
		BufferedReader reader;
		
		regex = formatRegex(regex);
		try{
			reader = new BufferedReader(new FileReader(acceptedWordsFile));
			while((word = reader.readLine()) != null){
				totalWords++;
				if (word.matches(regex) == accept) {
					passed++;
				} else {
					System.err.println(word);
					System.err.println("did" + (accept ? "n't" : "") + " match");
					System.err.println(regex);
				}
			}
			reader.close();
		} catch (Exception e){
			
		}
		return passed + "/" + totalWords;
	}
	
}
