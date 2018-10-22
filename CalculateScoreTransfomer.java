package org.sutter.neuroshare.transformer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

/**
 *
 * @author Charan This transformer is to transform the returning application
 *         code for solutions.
 *
 */
public class CalculateScoreTransfomer extends AbstractMessageTransformer {

	// All Variable Declaration Those are Used For Calculate Score.
	
	int vaod, vaos, scotoma, faceR, faceL, trigemR, trigemL, diplopia, speech, swallow, hear, powerRUE;
	int powerLUE, powerRLE, powerLLE, spasmRUE, spasmLUE, spasmRLE, spasmLLE;
	int tremorUE, tremorLE, balanceStand, balanceWalk, balanceSit, sensRUE, sensLUE, sensRLE, sensLLE;
	int bladderUrge, bladderLeak, bladderPad, bladderRetain, bowelConstipation;
	int bowelUrge, cognition, fatigue, fs2 = 0, fs3 = 0, fs4 = 0, fs5 = 0;
	int vision = 0, brainstem = 0, cerebellar = 0, pyramidal = 0, sensory = 0, bowlandBladder = 0;
	int cerebral = 0, ambulation = 0, bowl = 0;
	int q7_1 = 0, q7_2 = 0, q7_3 = 0, Q20 = 0, q5_1 = 0, q5_2 = 0, q6_1 = 0, q6_2 = 0, q8_1 = 0;
	int Q21 = 0, q25 = 0, q24_1 = 0, q24_2 = 0, q24_3 = 0, q24_4 = 0, q17_1 = 0, q17_2 = 0, q17_3 = 0, q17_4 = 0,
			Q21_1 = 0, Q21_2 = 0, q19 = 0;
	int q15 = 0, q14 = 0, q22 = 0, q23_1 = 0, q23_2 = 0, q23_3 = 0, q23_4 = 0, q23 = 0, q5_3 = 0;
	int q5_4 = 0, q6_3 = 0, q6_4 = 0, q8_2 = 0;
	int d1c = 0, d1b = 0, d1d = 0, c1 = 0, b1 = 0, a1 = 0, d1a = 0;
	int q24b = 0, q26 = 0, armuse = 0, leguse = 0, q2 = 0;
	String MILD = "Mild", MODERATE = "Moderate", SEVERE = "Severe", section = "", score = "";
	String getQxCode = "";
	

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		try {
			JSONArray mainJson = new JSONArray();
			
			JSONObject requestJson = message.getInvocationProperty("FullGetString");
			JSONObject responseJson = new JSONObject();
			Map<String, String> mapQuestionScore = new HashMap<String, String>();
		
			if (requestJson != null) {
			
				responseJson.put("pom_id", requestJson.getString("pom_id"));
				responseJson.putOpt("os", requestJson.optString("os"));
				responseJson.put("qx_started_at", requestJson.optString("qx_started_at"));
				responseJson.putOpt("status", requestJson.optString("status", null));
				responseJson.putOpt("qx_type", requestJson.optString("qx_type"));
				responseJson.put("qx_name", requestJson.optString("qx_name"));
				responseJson.put("qx_completed_at", requestJson.optString("qx_completed_at"));
				responseJson.putOpt("browser", requestJson.optString("browser"));
				//responseJson
				JSONArray responses = new JSONArray(requestJson.optString("responses", "[]"));
				if (responses != null && responses.length() > 0) {
					for (int i = 0; i < responses.length(); i++) {
						JSONObject jObjAttrDetails = responses.getJSONObject(i);
						try {
							if (jObjAttrDetails.has("qx_code")) {
								getQxCode = jObjAttrDetails.getString("qx_code");

							}
						} catch (Exception e) {
							this.logger.error(e);
						}
						//CV , 15-Oct-2018 , For Q1.4.1-Q1.4.4 Took Score from Answer_text Field.
						if(getQxCode.equalsIgnoreCase("Q1.4.1")  || getQxCode.equalsIgnoreCase("Q1.4.2") || getQxCode.equalsIgnoreCase("Q1.4.3") || getQxCode.equalsIgnoreCase("Q1.4.4")) {
							if(jObjAttrDetails.has("answer_text")) {
								mapQuestionScore.put(getQxCode,  jObjAttrDetails.getString("answer_text"));
							}
						}
						else {
						if (jObjAttrDetails.has("answer_text_score")) {
							Object objectAnswerTextScore = jObjAttrDetails.get("answer_text_score");

							String answerTextScore = "0";

							JSONArray jArrAnswerTextScore = new JSONArray();

							if (objectAnswerTextScore instanceof JSONArray) {
								jArrAnswerTextScore = (JSONArray) objectAnswerTextScore;
								for (int r = 0; r < jArrAnswerTextScore.length(); r++) {
									answerTextScore = jArrAnswerTextScore.get(r).toString();
								}

							} else {
								answerTextScore = jObjAttrDetails.optString("answer_text_score", "0");
							}

							mapQuestionScore.put(getQxCode, answerTextScore);
						}}

					}
					// Method To call FSS
					calculateFSS(responseJson, mapQuestionScore);
					// Method To Call Symptoms
					calculateSymptomScore(responseJson, mapQuestionScore);
					// Method to Call EDSS
					calculateEDSS(responseJson,mapQuestionScore);
				}

				mainJson.put(responseJson);
			}
			message.setInvocationProperty("CalculateScore", mainJson);
		} catch (Exception ex) {
			this.logger.error(ex);
		}
		return "";
	}
	// Below Method is Responsible For Convert String to Integer.
	public int convertintoInteger(String value) {
		try {
			if (value.contains("%")) {
				value = value.replaceAll("%", "");
				return Integer.parseInt(value);
			} else {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			return 0;
		}
	}
	// Below Method is Responsible For Calculation of Functional System Score.

	public void calculateFSS(JSONObject responseJson,Map<String, String> mapQuestionScore) {
		JSONArray functionalSystemArray = new JSONArray();
		// VISION
		vaod = convertintoInteger(mapQuestionScore.get("Q11.1"));
		vaos = convertintoInteger(mapQuestionScore.get("Q11.2"));
		scotoma = convertintoInteger(mapQuestionScore.get("Q11.3"));
		JSONObject visionFunctionSystem = new JSONObject();
		int visioncountZero = 0, visioncountOne = 0, visioncountTwo = 0, visioncountThree = 0;
		switch (vaod) {
		case 0:
			visioncountZero++;
			break;
		case 1:
			visioncountOne++;
			break;
		case 2:
			visioncountTwo++;
			break;
		case 3:
			visioncountThree++;
			break;
		}
		switch (vaos) {
		case 0:
			visioncountZero++;
			break;
		case 1:
			visioncountOne++;
			break;
		case 2:
			visioncountTwo++;
			break;
		case 3:
			visioncountThree++;
			break;
		}
		switch (scotoma) {
		case 0:
			visioncountZero++;
			break;
		case 1:
			visioncountOne++;
			break;
		case 2:
			visioncountTwo++;
			break;
		case 3:
			visioncountThree++;
			break;
		}

		// VAOD=0 and VAOS=0 and SCOTOMA=0
		if (vaod == 0 && vaos == 0 && scotoma == 0) {
			vision = 0;
		}
		// one or all of VAOD, VAOS, SCOTOMA=1, others=0 or 1
		else if (visioncountOne >= 1 && visioncountTwo == 0 && visioncountThree == 0) {
			vision = 1;
		}
	
		// New : 11-09-2018 VAOD, VAOS, SCOTOMA=2 or less
		else if ((vaod <= 2 && vaos <= 2 && scotoma <= 2)) {
			vision = 2;
		}

		
		// New : 11-09-2018 1 of VAOD and VOAS=3; all others=0, 1, or 2
		else if ((vaod == 3 && vaos < 3 && scotoma <= 2) || (vaod < 3 && vaos == 3 && scotoma <= 2)) {
			vision = 3;
		}
		
		// New : 11-09-2018 everything else
		else {
			vision = 4;
		}
		visionFunctionSystem.put("title", "Vision");
		visionFunctionSystem.put("score", String.valueOf(vision));
		functionalSystemArray.put(visionFunctionSystem);
		// BRAINSTEM
		faceR = convertintoInteger(mapQuestionScore.get("Q9.1"));
		faceL = convertintoInteger(mapQuestionScore.get("Q9.2"));
		trigemR = convertintoInteger(mapQuestionScore.get("Q10.1"));
		trigemL = convertintoInteger(mapQuestionScore.get("Q10.2"));
		diplopia = convertintoInteger(mapQuestionScore.get("Q12"));
		speech = convertintoInteger(mapQuestionScore.get("Q14"));
		swallow = convertintoInteger(mapQuestionScore.get("Q15"));
		// hear = convertintoInteger(mapQuestionScore.get("Q13"));

		// 9a=1, 9b=0, 10a=0, 10b=1, 12=1, 14=0, 15=1, 13=0

		JSONObject brainstemFunctionSystem = new JSONObject();
		int brainstemcountZero = 0, brainstemcountOne = 0, brainstemcountTwo = 0, brainstemcountThree = 0,
				brainstemcountFour = 0, brainstemcountFive = 0, brainstemcountSix = 0, brainstemcountSeven = 0,
				brainstemcountEight = 0;
		switch (faceR) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (faceL) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (trigemR) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (trigemL) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (diplopia) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (speech) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		switch (swallow) {
		case 0:
			brainstemcountZero++;
			break;
		case 1:
			brainstemcountOne++;
			break;
		case 2:
			brainstemcountTwo++;
			break;
		case 3:
			brainstemcountThree++;
			break;
		case 4:
			brainstemcountFour++;
			break;
		case 5:
			brainstemcountFive++;
			break;
		case 6:
			brainstemcountSix++;
			break;
		case 7:
			brainstemcountSeven++;
			break;
		case 8:
			brainstemcountEight++;
			break;
		}
		// switch (hear) {
		// case 0:
		// brainstemcountZero++;
		// break;
		// case 1:
		// brainstemcountOne++;
		// break;
		// case 2:
		// brainstemcountTwo++;
		// break;
		// case 3:
		// brainstemcountThree++;
		// break;
		// case 4:
		// brainstemcountFour++;
		// break;
		// case 5:
		// brainstemcountFive++;
		// break;
		// case 6:
		// brainstemcountSix++;
		// break;
		// case 7:
		// brainstemcountSeven++;
		// break;
		// case 8:
		// brainstemcountEight++;
		// break;
		// }

		block: {
			
			// New 26-09-2018:faceR=0 and faceL=0 and trigemR=0 and trigemL=0
			// and diplopia=0 and speech=0 and swallow=0
			if (faceR == 0 && faceL == 0 && trigemR == 0 && trigemL == 0 && diplopia == 0 && speech == 0
					&& swallow == 0) {
				brainstem = 0;
				break block;
			}

			// New 26-09-2018:
			// any or all(faceR,faceL,trigemR,trigemL,diplopia,speech,swallow)=1 and
			// Update 12th Oct 2018 , Change From brainstemcountOne == 1 to
			// brainstemcountOne >=1
			else if ((brainstemcountOne >= 1 && brainstemcountEight == 0 && brainstemcountFive == 0
					&& brainstemcountFour == 0 && brainstemcountSeven == 0 && brainstemcountSix == 0
					&& brainstemcountThree == 0 && brainstemcountTwo == 0)) {
				brainstem = 1;
				break block;
			}
			// New 26-09-2018"
			// [diplopia=2 and all other (faceR,faceL,speech,swallow)=0or1 and (trigemL or
			// trigemL=0,1,2] or
			// [any (faceR,faceL,trigemR,trigemL,speech,swallow)=1 anddiplopia=0,1]
			// or [trigemR or trigemL=2 and all other
			// (faceR,faceL,trigemR,trigemL,diplopia,speech,swallow=0or1]

			else if ((diplopia == 2 && faceR <= 1 && faceL <= 1 && speech <= 1 && swallow <= 1
					&& (trigemR <= 2 || trigemL <= 2))
					|| (diplopia <= 1 && (faceR == 1 || faceL == 1 || speech == 1 || swallow == 1 || trigemR == 1
							|| trigemL == 1))
					|| (diplopia <= 1 && faceR <= 1 && faceL <= 1 && speech <= 1 && swallow <= 1
							&& (trigemR == 2 || trigemL == 2))) {
				brainstem = 2;
				break block;
			}

			// New 26-09-2018:[diplopia=3 and all
			// other(faceR,faceL,trigemR,trigemL,swallow)=0,1or2
			// andspeech=0,1,2or3] or [any
			// (faceR,faceL,trigemR,trigemL,swallow)=2and speech=0,1,2or3 and
			// diplopia=0,1or2]
			else if ((diplopia == 3 && faceR <= 2 && faceL <= 2 && speech <= 3 && swallow <= 2 && trigemR <= 2
					&& trigemL <= 2)
					|| (diplopia <= 2 && speech <= 3
							&& (faceR == 2 || faceL == 2 || swallow == 2 || trigemR == 2 || trigemL == 2))) {
				brainstem = 3;
				break block;
			}

			
			// New 26-09-2018: speech=4 and all other
			// (faceR,faceL,trigemR,trigemL,diplopia,swallow)=0,1,2,or3] or[any
			// (faceR,faceL,trigemR,trigemL,diplopia,speech,swallow)=3]
			else if ((speech == 4 && diplopia <= 3 && faceR <= 3 && faceL <= 3 && swallow <= 3 && trigemR <= 3
					&& trigemL <= 3)
					|| (diplopia == 3 || speech == 3 || faceR == 3 || faceL == 3 || swallow == 3 || trigemR == 3
							|| trigemL == 3)) {
				brainstem = 4;
				break block;
			}

			// New 26-09-2018:speech=5 and all other
			// (faceR,faceL,trigemR,trigemL,diplopia,speech,swallow)=0,1,2,3,4
			else if (speech == 5 && diplopia <= 4 && faceR <= 4 && faceL <= 4 && swallow <= 4 && trigemR <= 4
					&& trigemL <= 4) {
				brainstem = 5;
				break block;
			}

		}
		brainstemFunctionSystem.put("title", "Brainstem");
		brainstemFunctionSystem.put("score", String.valueOf(brainstem));
		functionalSystemArray.put(brainstemFunctionSystem);
		// PYRAMIDAL
		powerRUE = convertintoInteger(mapQuestionScore.get("Q5.1"));
		powerLUE = convertintoInteger(mapQuestionScore.get("Q5.2"));
		powerRLE = convertintoInteger(mapQuestionScore.get("Q5.3"));
		powerLLE = convertintoInteger(mapQuestionScore.get("Q5.4"));
		spasmRUE = convertintoInteger(mapQuestionScore.get("Q6.1"));
		spasmLUE = convertintoInteger(mapQuestionScore.get("Q6.2"));
		spasmRLE = convertintoInteger(mapQuestionScore.get("Q6.3"));
		spasmLLE = convertintoInteger(mapQuestionScore.get("Q6.4"));
		JSONObject pyramidalFunctionalSystem = new JSONObject();
		// 11-09-2018 , Added CountFive , For FSS Scoring Q5 will have Upto 5
		// Score
		int countZero = 0, countOne = 0, countTwo = 0, countThree = 0, countFour = 0, countFive = 0;
		switch (powerRUE) {
		case 0:
			countZero++;
			break;
		case 1:
			countOne++;
			break;
		case 2:
			countTwo++;
			break;
		case 3:
			countThree++;
			break;
		case 4:
			countFour++;
			break;
		case 5:
			countFive++;
			break;
		}
		switch (powerLUE) {
		case 0:
			countZero++;
			break;
		case 1:
			countOne++;
			break;
		case 2:
			countTwo++;
			break;
		case 3:
			countThree++;
			break;
		case 4:
			countFour++;
			break;
		case 5:
			countFive++;
			break;
		}
		switch (powerRLE) {
		case 0:
			countZero++;
			break;
		case 1:
			countOne++;
			break;
		case 2:
			countTwo++;
			break;
		case 3:
			countThree++;
			break;
		case 4:
			countFour++;
			break;
		case 5:
			countFive++;
			break;
		}
		switch (powerLLE) {
		case 0:
			countZero++;
			break;
		case 1:
			countOne++;
			break;
		case 2:
			countTwo++;
			break;
		case 3:
			countThree++;
			break;
		case 4:
			countFour++;
			break;
		case 5:
			countFive++;
			break;
		}
		ArrayList<Integer> arrMaxValueofQ6 = new ArrayList<Integer>();
		arrMaxValueofQ6.add(spasmRUE);
		arrMaxValueofQ6.add(spasmLUE);
		arrMaxValueofQ6.add(spasmRLE);
		arrMaxValueofQ6.add(spasmLLE);

		int maxvalueofq6 = Collections.max(arrMaxValueofQ6);

		// We have to change the order of conditional statements because the
		// requirement concluded to execute a single if statement and skip the
		// rest of the conditional statements.
		// If we had used else-if ladder then every condition would be checked
		// in the worst case scenario. In the best case scenario, if the first
		// condition is satisfied then it's statements will be executed and the
		// rest of the conditions will be skipped. Although we would want the
		// code to execute the second condition's statements but because the
		// second condition is a superset of the first condition, if the first
		// is satisfied it would not go to the second to check additional
		// statements.
		// Charan - Replaced or with and conditions here
		block: {

			// all=0
			if (powerRUE == 0 && powerLUE == 0 && powerRLE == 0 && powerLLE == 0) {
				pyramidal = 0;
				break block;
			}

			

			// New : 11-09-2018 [2 or less of
			// powerRUE,powerLUE,powerRLE,powerLLE=1 and max value=1] AND [max
			// value of spasmRUE,spasmLUE,spasmRLE,spasmLLE=1]
			else if ((countOne > 0 && countOne <= 2 && countTwo == 0 && countThree == 0 && countFour == 0
					&& countFive == 0) && (maxvalueofq6 == 1)) {
				pyramidal = 1;
				break block;
			}

			
			// New: 11-09-2018 [3 or less of
			// powerRUE,powerLUE,powerRLE,powerLLE=2 and max value=2] AND [max
			// value of spasmRUE,spasmLUE,spasmRLE,spasmLLE>=1]
			else if ((countTwo > 0 && countTwo <= 3 && countThree == 0 && countFour == 0 && countFive == 0)
					&& (maxvalueofq6 >= 1)) {
				pyramidal = 2;
				break block;
			}
			
			// New: 11-09-2018 [3 or more of
			// powerRUE,powerLUE,powerRLE,powerLLE=1 and max value=1]
			// OR [4 or less of powerRUE,powerLUE,powerRLE,powerLLE=2 and max
			// value=2]
			// OR [1 of powerRUE,powerLUE,powerRLE,powerLLE=3, and max value=3]
			else if ((countOne >= 3 && countTwo == 0 && countThree == 0 && countFour == 0 && countFive == 0)
					|| (countTwo > 0 && countTwo <= 4 && countThree == 0 && countFour == 0 && countFive == 0)
					|| (countThree == 1 && countFour == 0 && countFive == 0)) {
				pyramidal = 3;
				break block;
			}

			
			

			// New : 11-09-2018 [3 of powerRUE,powerLUE,powerRLE,powerLLE=3 and
			// max value=3]
			// OR [1 of powerRUE,powerLUE,powerRLE or powerLLE=4 and 1
			// or less powerRUE,powerLUE,powerRLE or powerLLE<3 and max value=4]
			// OR [3 or more of powerRUE,powerLUE,powerRLE,powerLLE=3 and max
			// value=3]
			else if ((countThree == 3 && countFour == 0 && countFive == 0)
					|| ((countFour == 1 || countThree >= 3) && countFive == 0)
					|| (countThree >= 3 && countFour == 0 && countFive == 0)) {
				pyramidal = 4;
				break block;
			}
			

			// New : 11-09-2018 [2 of powerRUE,powerLUE,powerRLE,powerLLE=4 and
			// max value=4]
			// OR [powerRUE,powerLUE,powerRLE,powerLLE=3]
			else if ((countFour == 2 && countFive == 0) || countThree == 4) {
				pyramidal = 5;
				break block;
			}

			
			// New : 11-09-2018 Everything else 6
			else {
				pyramidal = 6;
				break block;
			}
		}
		pyramidalFunctionalSystem.put("title", "Pyramidal");
		pyramidalFunctionalSystem.put("score", String.valueOf(pyramidal));
		functionalSystemArray.put(pyramidalFunctionalSystem);
		// CEREBRAL
		// fatigue = convertintoInteger(mapQuestionScore.get("Q21"));
		// New : 03-10-2018 : fatigue = convertintoInteger(mapQuestionScore.get("Q21"));
		cognition = convertintoInteger(mapQuestionScore.get("Q20"));
		fatigue = convertintoInteger(mapQuestionScore.get("Q21"));
		JSONObject cerebralFunctionalSystem = new JSONObject();
		// cognition=0, fatigue=0
		if (cognition == 0 && fatigue == 0) {
			cerebral = 0;
		}
		
		// New : 11-09-2018 cognition<=1 and fatigue==1
		// New : 26-09-2018 cognition==0 and fatigue>=1
		else if (cognition <= 0 && fatigue >= 1) {
			cerebral = 1;
		}
		
		// New : 11-09-2018 cognition<=1 and fatigue<=3
		// New : 26-09-2018 cognition==1 and fatigue<=3
		else if (cognition == 1 && fatigue <= 3) {
			cerebral = 2;
		}
		
		// New : 11-09-2018 cognition<=2 and fatigue<=3
		// New : 26-09-2018 cognition==2 and fatigue<=3
		else if (cognition == 2 && fatigue <= 3) {
			cerebral = 3;
		}
		
		// New : 11-09-2018 cognition<=3 and fatigue<=3
		// New : 26-09-2018 cognition==3 and fatigue<=3
		else if (cognition == 3 && fatigue <= 3) {
			cerebral = 4;
		}
		
		// New : 11-09-2018 everything else
		else {
			cerebral = 5;
		}
		cerebralFunctionalSystem.put("title", "Cerebral");
		cerebralFunctionalSystem.put("score", String.valueOf(cerebral));
		functionalSystemArray.put(cerebralFunctionalSystem);
		// CEREBELLAR

		// We have to change the order of conditional statements because the
		// requirement concluded to execute a single if statement and skip the
		// rest of the conditional statements.
		// If we had used else-if ladder then every condition would be checked
		// in the worst case scenario. In the best case scenario, if the first
		// condition is satisfied then it's statements will be executed and the
		// rest of the conditions will be skipped. Although we would want the
		// code to execute the second condition's statements but because the
		// second condition is a superset of the first condition, if the first
		// is satisfied it would not go to the second to check additional
		// statements.

		tremorUE = convertintoInteger(mapQuestionScore.get("Q8.1"));
		tremorLE = convertintoInteger(mapQuestionScore.get("Q8.2"));
		balanceStand = convertintoInteger(mapQuestionScore.get("Q7.1"));
		balanceWalk = convertintoInteger(mapQuestionScore.get("Q7.2"));
		balanceSit = convertintoInteger(mapQuestionScore.get("Q7.3"));

		JSONObject cerebellarFunctionalSystem = new JSONObject();

		int cerebellarcountZero = 0, cerebellarcountOne = 0, cerebellarcountTwo = 0, cerebellarcountThree = 0,
				cerebellarcountFour = 0;
		switch (tremorUE) {
		case 0:
			cerebellarcountZero++;
			break;
		case 1:
			cerebellarcountOne++;
			break;
		case 2:
			cerebellarcountTwo++;
			break;
		case 3:
			cerebellarcountThree++;
			break;
		case 4:
			cerebellarcountFour++;
			break;
		}
		switch (tremorLE) {
		case 0:
			cerebellarcountZero++;
			break;
		case 1:
			cerebellarcountOne++;
			break;
		case 2:
			cerebellarcountTwo++;
			break;
		case 3:
			cerebellarcountThree++;
			break;
		case 4:
			cerebellarcountFour++;
			break;
		}
		switch (balanceStand) {
		case 0:
			cerebellarcountZero++;
			break;
		case 1:
			cerebellarcountOne++;
			break;
		case 2:
			cerebellarcountTwo++;
			break;
		case 3:
			cerebellarcountThree++;
			break;
		case 4:
			cerebellarcountFour++;
			break;
		}
		switch (balanceWalk) {
		case 0:
			cerebellarcountZero++;
			break;
		case 1:
			cerebellarcountOne++;
			break;
		case 2:
			cerebellarcountTwo++;
			break;
		case 3:
			cerebellarcountThree++;
			break;
		case 4:
			cerebellarcountFour++;
			break;
		}
		switch (balanceSit) {
		case 0:
			cerebellarcountZero++;
			break;
		case 1:
			cerebellarcountOne++;
			break;
		case 2:
			cerebellarcountTwo++;
			break;
		case 3:
			cerebellarcountThree++;
			break;
		case 4:
			cerebellarcountFour++;
			break;
		}

		ArrayList<Integer> arrMaxValueofQ8 = new ArrayList<Integer>();
		arrMaxValueofQ8.add(tremorUE);
		arrMaxValueofQ8.add(tremorLE);

		int maxvalueofq8 = Collections.max(arrMaxValueofQ8);

		block: {
			if (tremorUE == 0 && tremorLE == 0 && balanceStand == 0 && balanceWalk == 0 && balanceSit == 0) {
				cerebellar = 0;
				break block;
			}
			// 1 or more of tremorUE, tremorLE, balanceStand,
			// balancewalk,balancesit=1; others=0
			// 1 or more of tremorUE, tremorLE, balancestand, balancewalk, balancesit=1;
			// others=0
			// 8a=1, 8b=1, 7a=1, 7b=1, 7c=1
			else if (cerebellarcountOne >= 1 && cerebellarcountTwo == 0 && cerebellarcountThree == 0
					&& cerebellarcountFour == 0) {
				cerebellar = 1;
				break block;
			}
			// 1 or more of tremorUE, tremorLE, balanceStand,
			// balancewalk,balancesit=2; others=0,1
			else if (cerebellarcountTwo >= 1 && cerebellarcountThree == 0 && cerebellarcountFour == 0) {
				cerebellar = 2;
				break block;
			}
			// 1 or more of
			// tremorUE,tremorLE,balancesit,balancewalk,balanceStand=3;
			// Others=0,1,2
			else if (cerebellarcountThree >= 1 && cerebellarcountFour == 0) {
				cerebellar = 3;
				break block;
			}

			// New : 11-09-2018 (tremorUE or tremorLE=3 and max value=3) AND
			// (balancestand=3 or balancesit=4 or balancewalk=4)
			else if (((tremorUE == 3 || tremorLE == 3) && maxvalueofq8 == 3)
					&& (balanceStand == 3 || balanceSit == 4 || balanceWalk == 4)) {
				cerebellar = 4;
				break block;
			}
			// New : 11-09-2018 everything else
			else {
				cerebellar = 5;
				break block;
			}
		}

		cerebellarFunctionalSystem.put("title", "Cerebellar");
		cerebellarFunctionalSystem.put("score", String.valueOf(cerebellar));
		functionalSystemArray.put(cerebellarFunctionalSystem);
		// SENSORY
		// We have to change the order of conditional statements because the
		// requirement concluded to execute a single if statement and skip the
		// rest of the conditional statements.
		// If we had used else-if ladder then every condition would be checked
		// in the worst case scenario. In the best case scenario, if the first
		// condition is satisfied then it's statements will be executed and the
		// rest of the conditions will be skipped. Although we would want the
		// code to execute the second condition's statements but because the
		// second condition is a superset of the first condition, if the first
		// is satisfied it would not go to the second to check additional
		// statements.
		sensRUE = convertintoInteger(mapQuestionScore.get("Q4.1"));
		sensLUE = convertintoInteger(mapQuestionScore.get("Q4.2"));
		sensRLE = convertintoInteger(mapQuestionScore.get("Q4.3"));
		sensLLE = convertintoInteger(mapQuestionScore.get("Q4.4"));
		JSONObject sensoryFunctionalSystem = new JSONObject();
		int sensorycountZero = 0, sensorycountOne = 0, sensorycountTwo = 0, sensorycountThree = 0, sensorycountFour = 0;
		switch (sensRUE) {
		case 0:
			sensorycountZero++;
			break;
		case 1:
			sensorycountOne++;
			break;
		case 2:
			sensorycountTwo++;
			break;
		case 3:
			sensorycountThree++;
			break;
		case 4:
			sensorycountFour++;
			break;
		}
		switch (sensLUE) {
		case 0:
			sensorycountZero++;
			break;
		case 1:
			sensorycountOne++;
			break;
		case 2:
			sensorycountTwo++;
			break;
		case 3:
			sensorycountThree++;
			break;
		case 4:
			sensorycountFour++;
			break;
		}
		switch (sensRLE) {
		case 0:
			sensorycountZero++;
			break;
		case 1:
			sensorycountOne++;
			break;
		case 2:
			sensorycountTwo++;
			break;
		case 3:
			sensorycountThree++;
			break;
		case 4:
			sensorycountFour++;
			break;
		}
		switch (sensLLE) {
		case 0:
			sensorycountZero++;
			break;
		case 1:
			sensorycountOne++;
			break;
		case 2:
			sensorycountTwo++;
			break;
		case 3:
			sensorycountThree++;
			break;
		case 4:
			sensorycountFour++;
			break;
		}
		block: {
			// sensRUE,sensLUE,sensRLE,sensLLE=0
			if (sensRUE == 0 && sensLUE == 0 && sensRLE == 0 && sensLLE == 0) {
				sensory = 0;
				break block;
			}
			// 1 or 2 of: sensRUE,sensLUE,sensRLE,sensLLE=1, others=0
			else if ((sensorycountOne == 1 && sensorycountZero == 3)
					|| (sensorycountOne == 2 && sensorycountZero == 2)) {
				sensory = 1;
				break block;
			}
			// [1 or 2 of: sensRUE,sensLUE,sensRLE,sensLLE=2, others=0,1] or [3
			// or 4 of sensRUE,sensLUE,sensRLE,sensLLE=1, others=0]
			else if (((sensorycountTwo == 1 || sensorycountTwo == 2) && sensorycountThree == 0 && sensorycountFour == 0)
					|| ((sensorycountOne == 3 || sensorycountOne == 4) && sensorycountTwo == 0 && sensorycountThree == 0
							&& sensorycountFour == 0)) {
				sensory = 2;
				break block;
			}
			// [1 or 2 of sensRUE,sensLUE,sensRLE,sensLLE=3, others=0,1or2] or
			// [3 or 4 of sensRUE,sensLUE,sensRLE,sensLLE=2, others 0,1]
			else if (((sensorycountThree == 1 || sensorycountThree == 2) && sensorycountFour == 0)
					|| ((sensorycountTwo == 3 || sensorycountTwo == 4) && sensorycountThree == 0
							&& sensorycountFour == 0)) {
				sensory = 3;
				break block;
			}
			// 2 or more of sensRUE,sensLUE,sensRLE,sensLLE=3; others=0,1or2
			else if ((sensorycountThree >= 2 && sensorycountFour == 0)) {
				sensory = 4;
				break block;
			}

			// [1 or 2 of:sensRUE,sensLUE,sensRLE,sensLLE=4, others =0,1,2or3]
			// or [sensRUE,sensLUE,sensRLE,sensLLE=3]
			else if ((sensorycountFour == 1 || sensorycountFour == 2 || sensorycountThree == 4)) {
				sensory = 5;
				break block;
			}

			// sensRUE,sensLUE,sensRLE,sensLLE=4
			else if (sensRUE == 4 && sensLUE == 4 && sensRLE == 4 && sensLLE == 4) {
				sensory = 6;
				break block;
			}
		}
		sensoryFunctionalSystem.put("title", "Sensory");
		sensoryFunctionalSystem.put("score", String.valueOf(sensory));
		functionalSystemArray.put(sensoryFunctionalSystem);
		// BOWELANDBLADDE
		bladderPad = convertintoInteger(mapQuestionScore.get("Q18"));
		bladderRetain = convertintoInteger(mapQuestionScore.get("Q19"));
		// New 03-10-2018:bladderPad =
		// convertintoInteger(mapQuestionScore.get("Q17.3"));,bladderRetain =
		// convertintoInteger(mapQuestionScore.get("Q17.4"));

		bladderUrge = convertintoInteger(mapQuestionScore.get("Q17.1"));
		bladderLeak = convertintoInteger(mapQuestionScore.get("Q17.2"));

		bowelUrge = convertintoInteger(mapQuestionScore.get("Q16.2"));
		bowelConstipation = convertintoInteger(mapQuestionScore.get("Q16.1"));
		JSONObject bowlandBladderFunctionalSystem = new JSONObject();
		int bowlandbladdercountzero = 0, bowlandbladdercountfirst = 0, bowlandbladdercountwo = 0,
				bowlandbladdercountthree = 0, bowlandbladdercountfour = 0, bowlandbladdercountfive = 0,
				bowlandbladdercountsix = 0;
		switch (bladderUrge) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		switch (bladderLeak) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		switch (bladderPad) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		switch (bladderRetain) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		switch (bowelUrge) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		switch (bowelConstipation) {
		case 0:
			bowlandbladdercountzero++;
			break;
		case 1:
			bowlandbladdercountfirst++;
			break;
		case 2:
			bowlandbladdercountwo++;
			break;
		case 3:
			bowlandbladdercountthree++;
			break;
		case 4:
			bowlandbladdercountfour++;
			break;
		case 5:
			bowlandbladdercountfive++;
			break;
		case 6:
			bowlandbladdercountsix++;
			break;
		}
		// bladderUrge,bladderLeak,bladderPad,bladderRetain,bowelConstipation,BowelFrequency=0
		if (bladderUrge == 0 && bladderLeak == 0 && bladderPad == 0 && bladderRetain == 0 && bowelConstipation == 0
				&& bowelUrge == 0) {
			bowlandBladder = 0;
		}
		
		// New : 11-09-2018 [1 or more of:
		// BladderLeak,BladderRetain,BowelConstipation,BowelUrge=1,
		// BladderUrge>=1, others=0] and [BladderPad=0]
		//New : 16-10-2018 
		//if any BladderUrge,BladderLeak,BladderPad,BladderRetain,BowelConstipation,BowelFrequency <= 1 and check_pad == 0  fss 1
		else if ((bladderUrge <= 1 ||bladderLeak <= 1 ||bladderPad <= 1 || bladderRetain <= 1 || bowelConstipation <=1 || bowelUrge <= 1) && bladderPad == 0)
		{
			bowlandBladder = 1;
		}

		// Solved : Oct122018 : Added Missing COondition - bowel_bladder_max_value&.<= 2
		// ((bladder_leak == 2 and pad == 1) or (bladder_retain == 2) or ((bowel_urge ==
		// 2) and (constipation&.<= 1)))
		// and bowel_bladder_max_value&.<= 2
		else if (((bladderLeak == 2 && bladderPad == 1) || (bladderRetain == 2)
				|| ((bladderUrge == 2) && bowelConstipation <= 1))
				&& (bowlandbladdercountthree == 0 && bowlandbladdercountfour == 0 && bowlandbladdercountsix == 0
						&& bowlandbladdercountfive == 0)) {
			bowlandBladder = 2;
		}
		// [BladderUrge=3or4, BladderLeak=3or4, pad=1] or [bladderRetain=3];
		// andConstipation=2; bowelUrge = 2
		// Solved : 12thOct2018 , ReVerfiy Brackets
		// [BladderUrge=3or4, BladderLeak=3or4, pad=1] or [BladderRetain=3]; and
		// Constipation=2; Bowelurge = 2
		else if ((((bladderLeak >= 3 || bladderLeak <= 4) && (bladderUrge >= 3 || bladderUrge <= 4) && bladderPad == 1)
				|| bladderRetain == 3) && bowelConstipation == 2 && bowelUrge == 2) {
			bowlandBladder = 3;
		}

		// [BladderUrge=3or4, BladderLeak=3or4, pad=1] or [BladderRetain=3]; and
		// Constipation=2; Bowelurge = 2
		// Solved : Added BladderUrge>0 Condition To Avoid 0 Value for that
		else if ((bladderRetain >= 3 || bladderRetain <= 4) && bladderUrge <= 3 && bladderUrge > 0 && bladderPad == 1
				&& bowelConstipation <= 1 && bowelUrge <= 2) {
			bowlandBladder = 3;
		}
		// [bowel urge=3, constipation=2] or [bladderurgency=4 and/or
		// bladderRetain=5,and pad=1]
		else if ((bowelUrge == 3 && bowelConstipation == 2)
				|| ((bladderUrge == 4 || bladderRetain == 5) && bladderPad == 1)) {
			bowlandBladder = 4;
		}
		// [bowel urge=3, constipation=2] and [bladderurgency=4 and/or
		// bladderRetain=5,and pad=1]
		// [bowel urge=3, constipation=2] and [bladderurgency=4 and/or bladder
		// retain=5,and pad=1]
		else if ((bowelUrge == 3 && bowelConstipation == 2)
				&& ((bladderUrge == 4 || bladderRetain == 5) && bladderPad == 1)) {
			bowlandBladder = 5;
		}
		bowlandBladderFunctionalSystem.put("title", "Bowel and Bladder");
		bowlandBladderFunctionalSystem.put("score", String.valueOf(bowlandBladder));
		functionalSystemArray.put(bowlandBladderFunctionalSystem);
		// BOWEL
		// constipation=0, bowel urge=0
		if (bowelUrge == 0 && bowelConstipation == 0) {
			bowl = 0;
		}
		// constipation=1 and bowel frequency=0,1, or constipation=0 and bowel
		// frequency=1
		else if ((bowelConstipation == 1 && bowelUrge == 1) || (bowelConstipation == 1 && bowelUrge == 0)
				|| (bowelConstipation == 0 && bowelUrge == 1)) {
			bowl = 1;
		}
		// bowel urge=2; constipation=0,1
		else if (bowelUrge == 2 && bowelConstipation <= 1) {
			bowl = 2;
		}
		// constipation=2; bowel urge=0,1,2
		else if (bowelConstipation == 2 && bowelUrge <= 2) {
			bowl = 3;
		}
		// bowel urge=3; constipation=01,2
		else if (bowelUrge == 3 && bowelConstipation <= 2) {
			bowl = 4;
		}
		// Ambulation
		a1 = convertintoInteger(mapQuestionScore.get("Q1.1"));
		b1 = convertintoInteger(mapQuestionScore.get("Q1.2"));
		c1 = convertintoInteger(mapQuestionScore.get("Q1.3"));
		d1a = convertintoInteger(mapQuestionScore.get("Q1.4.1"));
		d1b = convertintoInteger(mapQuestionScore.get("Q1.4.2"));
		d1c = convertintoInteger(mapQuestionScore.get("Q1.4.3"));
		d1d = convertintoInteger(mapQuestionScore.get("Q1.4.4"));

		// ---------------- q2=question 2 ------------------
		q2 = convertintoInteger(mapQuestionScore.get("Q2"));
		JSONObject AmbulationFunctionalSystem = new JSONObject();
		// 1a=1
		ambulation = 0;
		if (a1 == 1) {
			ambulation = 0;
		}
		
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=2
		// Update : 22thOct2018 New Spec -  1a=2 and 1b=1
		else if (a1 == 2 && b1 == 1) {
			ambulation = 1;
		}
		// OLD Spec - 1b>=2 or 1b<=3
		// Update : 16thOct2018 New Spec -  1a=2 and 1b>=2 and 1b<=3
		else if (a1 == 2 && b1 >= 2 && b1 <= 3) {
			ambulation = 2;
		}


		// Change : Charan - 15-10-2018:1b=4 and 1d.A>=20 and (1d.C+1d.D)<=10
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=4 and 1d.A>=20 and (1d.C+1d.D)<=10
		else if (a1 == 2 && b1 == 4 && d1a >= 20 && (d1c + d1d) <= 10) {
			ambulation = 3;
		}
		// 1b=4 and 1d.A>=20 and (1d.C+1d.D)>=10
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=4 and 1d.A>=20 and (1d.C+1d.D)>=10
		else if (a1 == 2 && b1 == 4 && d1a >= 20 && (d1c + d1d) >= 10) {
			ambulation = 4;
		}
		// 1b=4 and 1d.A<20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=4 and 1d.A<20
		else if (a1 == 2 && b1 == 4 && d1a < 20) {
			ambulation = 4;
		}
		// 1c=1 and (1d.C+1d.D)<50 and 1d.D<=20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c=1 and (1d.C+1d.D)<50 and 1d.D<=20
		else if (a1 == 2 && b1 == 5 && c1 == 1 && (d1c + d1d) < 50 && d1d <= 20) {
			ambulation = 4;
		}
		// 1c=1 and (1d.C+1d.D)>=50 and 1d.D<=20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c=1 and (1d.C+1d.D)>=50 and 1d.D<=20
		else if (a1 == 2 && b1 == 5 && c1 == 1 && (d1c + d1d) >= 50 && d1d <= 20) {
			ambulation = 5;
		}
		// 1c=1 and 1d.D>20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c=1 and 1d.D>20
		else if (a1 == 2 && b1 == 5 && c1 == 1 && d1d > 20) {
			ambulation = 6;
		}
		// 1c=1 and (1d.A+1d.B)>=80
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c=1 and (1d.A+1d.B)>=80
		else if (a1 == 2 && b1 == 5 && c1 == 1 && (d1a + d1b) >= 80) {
			ambulation = 4;
		}
		// 1c=1 and 1d.A>=80
		// Update : 16thOct2018 New Spec - 1a=2 and 1b=5 and 1c=1 and 1d.A>=80
		else if (a1 == 2 && b1 == 5 && c1 == 1 && d1a >= 80) {
			ambulation = 3;
		}
		// 1c>=2 and 1c<=3 and (1d.C+1d.D)<50 and 1d.D<=20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c>=2 and 1c<=3 and (1d.C+1d.D)<50 and 1d.D<=20
		else if (a1 == 2 && b1 == 5  && c1 >= 2 && c1 <= 3 && (d1c + d1d) < 50 && d1d <= 20) {
			ambulation = 5;
		}
		// 1c>=2 and 1c<=3 and (1d.C+1d.D)<50 and 1d.D>20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c>=2 and 1c<=3 and (1d.C+1d.D)<50 and 1d.D>20
		else if (a1 == 2 && b1 == 5  && c1 >= 2 && c1 <= 3 && (d1c + d1d) < 50 && d1d > 20) {
			ambulation = 6;
		}
		// 1c>=2 and 1c<=3 and (1d.C+1d.D)>=50 and 1d.D<=20
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c>=2 and 1c<=3 and (1d.C+1d.D)>=50 and 1d.D<=20
		else if (a1 == 2 && b1 == 5  && c1 >= 2 && c1 <= 3 && (d1c + d1d) >= 50 && d1d <= 20) {
			ambulation = 6;
		}
		// 1c>=2 and 1c<=3 and (1d.C+1d.D)>=50 and 1d.D>20
		// Update : 16thOct2018 New Spec -   1a=2 and 1b=5 and 1c>=2 and 1c<=3 and (1d.C+1d.D)>=50 and 1d.D>20
		else if (a1 == 2 && b1 == 5  && c1 >= 2 && c1 <= 3 && (d1c + d1d) >= 50 && d1d > 20) {
			ambulation = 7;
		} 
		// 1c>=2 and 1c<=3 and (1d.A+1d.B)>=80
		// Update : 16thOct2018 New Spec -  1a=2 and 1b=5 and 1c>=2 and 1c<=3 and (1d.A+1d.B)>=80
		else if (a1 == 2 && b1 == 5  && c1 >= 2 && c1 <= 3 && (d1a + d1b) >= 80) {
			ambulation = 4;
		}
		// 1c>=2 and 1c<=3 and 1d.A>=80
		// Update : 16thOct2018 New Spec - 1a=2 and 1b=5 and 1c>=2 and 1c<=3 and 1d.A>=80
		else if (a1 == 2 && b1 == 5 && c1 >= 2 && c1 <= 3 && d1a >= 80) {
			ambulation = 3;
		}
		// 1a=3 and 2<=2
		else if (a1 == 3 && q2 <= 2) {
			ambulation = 7;
		}
		// 1a=3 and 2>2
		else if (a1 == 3 && q2 > 2) {
			ambulation = 8;
		}
		// 1a=4 and 2<=4
		else if (a1 == 4 && q2 <= 4) {
			ambulation = 8;
		}
		// 1a=4 and 2=5
		else if (a1 == 4 && q2 == 5) {
			ambulation = 9;
		}
		
		AmbulationFunctionalSystem.put("title", "Ambulation");
		AmbulationFunctionalSystem.put("score", String.valueOf(ambulation));
		functionalSystemArray.put(AmbulationFunctionalSystem);
		responseJson.put("functional_system_scores", functionalSystemArray);
	}

	// Below Methos is Responsible for Symptom Score Calculation
	public void calculateSymptomScore(JSONObject responseJson,Map<String, String> mapQuestionScore) {
		JSONArray symptomsArray = new JSONArray();
		JSONArray tmpQxCodeArray = new JSONArray();
		JSONObject symptomsScoreQ20 = new JSONObject();
		Q20 = convertintoInteger(mapQuestionScore.get("Q20"));

		if (Q20 == 0) {
			score = "";
		} else if (Q20 == 1) {
			score = MILD;
		} else if (Q20 == 2) {
			score = MODERATE;
		} else if (Q20 == 3 || Q20 == 4) {
			score = SEVERE;
		}
		symptomsScoreQ20.put("title", "Cognition");
		symptomsScoreQ20.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q20");
		symptomsScoreQ20.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ20);
		JSONObject symptomsScoreQ21 = new JSONObject();
		// for Fatigue symptomps score using the Question number Q21.1
		score = "";
		Q21_1 = convertintoInteger(mapQuestionScore.get("Q21"));

		switch (Q21_1) {
		case 0:
			score = "";
			break;
		case 1:
			score = MILD;
			break;
		case 2:
			score = MODERATE;
			break;
		case 3:
			score = SEVERE;
			break;
		}
		symptomsScoreQ21.put("title", "Fatigue");
		symptomsScoreQ21.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q21");
		symptomsScoreQ21.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ21);
		JSONObject symptomsScoreQ23 = new JSONObject();
		q23_1 = convertintoInteger(mapQuestionScore.get("Q23.1"));
		q23_2 = convertintoInteger(mapQuestionScore.get("Q23.2"));
		q23_3 = convertintoInteger(mapQuestionScore.get("Q23.3"));
		q23_4 = convertintoInteger(mapQuestionScore.get("Q23.4"));
		q23 = q23_1 + q23_2 + q23_3 + q23_4;
		score = "";
		if (q23 == 0 || q23 == 1 || q23 == 2) {
			score = "";
		} else if (q23 == 3 || q23 == 4 || q23 == 5) {
			score = MILD;
		} else if (q23 == 6 || q23 == 7 || q23 == 8) {
			score = MODERATE;
		} else if (q23 == 9 || q23 == 10 || q23 == 11 || q23 == 12) {
			score = SEVERE;
		}
		symptomsScoreQ23.put("title", "Mood");
		symptomsScoreQ23.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q23.1");
		tmpQxCodeArray.put("Q23.2");
		tmpQxCodeArray.put("Q23.3");
		tmpQxCodeArray.put("Q23.4");
		
		symptomsScoreQ23.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ23);
		JSONObject symptomsScoreQ22 = new JSONObject();
		score = "";
		q22 = convertintoInteger(mapQuestionScore.get("Q22"));
		if (q22 == 0 || q22 == 1 || q22 == 2) {
			score = "";
		} else if (q22 == 3) {
			score = MILD;
		} else if (q22 == 4) {
			score = MODERATE;
		} else if (q22 == 5 || q22 == 6) {
			score = SEVERE;
		}
		symptomsScoreQ22.put("title", "Sleep");
		symptomsScoreQ22.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q22");
		symptomsScoreQ22.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ22);
		JSONObject symptomsScoreQ11 = new JSONObject();
		int temp11 = vision;
		score = "";
		if (temp11 == 0 || temp11 == 1) {
			score = "";
		} else if (temp11 == 2) {
			score = MILD;
		} else if (temp11 == 3) {
			score = MODERATE;
		} else if (temp11 == 4) {
			score = SEVERE;
		}
		symptomsScoreQ11.put("title", "Vision");
		symptomsScoreQ11.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q11.1");
		tmpQxCodeArray.put("Q11.2");
		tmpQxCodeArray.put("Q11.3");
		symptomsScoreQ11.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ11);
		JSONObject symptomsScoreQ14 = new JSONObject();
		q14 = convertintoInteger(mapQuestionScore.get("Q14"));
		score = "";
		if (q14 == 0 || q14 == 1) {
			score = "";
		} else if (q14 == 2) {
			score = MILD;
		} else if (q14 == 3) {
			score = MODERATE;
		} else if (q14 == 4 || q14 == 5) {
			score = SEVERE;
		}
		symptomsScoreQ14.put("title", "Speech");
		symptomsScoreQ14.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q14");
		symptomsScoreQ14.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ14);
		JSONObject symptomsScoreQ15 = new JSONObject();
		q15 = convertintoInteger(mapQuestionScore.get("Q15"));
		score = "";
		if (q15 == 0) {
			score = "";
		} else if (q15 == 1) {
			score = MODERATE;
		} else if (q15 == 2 || q15 == 3) {
			score = SEVERE;
		}
		symptomsScoreQ15.put("title", "Swallowing");
		symptomsScoreQ15.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q15");
		symptomsScoreQ15.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ15);
		JSONObject symptomsScoreArmUse = new JSONObject();
		q5_1 = convertintoInteger(mapQuestionScore.get("Q5.1"));
		q5_2 = convertintoInteger(mapQuestionScore.get("Q5.2"));
		q6_1 = convertintoInteger(mapQuestionScore.get("Q6.1"));
		q6_2 = convertintoInteger(mapQuestionScore.get("Q6.2"));
		q8_1 = convertintoInteger(mapQuestionScore.get("Q8.1"));
		ArrayList<Integer> armUsearrayList = new ArrayList<Integer>();
		armUsearrayList.add(q5_1);
		armUsearrayList.add(q5_2);
		armUsearrayList.add(q6_1);
		armUsearrayList.add(q6_2);
		armUsearrayList.add(q8_1);
		int armuse = Collections.max(armUsearrayList);
		score = "";
		if (armuse == 0 || armuse == 1) {
			score = "";
		} else if (armuse == 2) {
			score = MILD;
		} else if (armuse == 3) {
			score = MODERATE;
		}
		// 11-09-2018 , Update From 4 to 4,5
		else if (armuse == 4 || armuse == 5) {
			score = SEVERE;
		}
		symptomsScoreArmUse.put("title", "Arm use");
		symptomsScoreArmUse.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q5.1");
		tmpQxCodeArray.put("Q5.2");
		tmpQxCodeArray.put("Q6.1");
		tmpQxCodeArray.put("Q6.2");
		tmpQxCodeArray.put("Q8.1");
		symptomsScoreArmUse.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreArmUse);
		JSONObject symptomsScoreLegUse = new JSONObject();
		q5_3 = convertintoInteger(mapQuestionScore.get("Q5.3"));
		q5_4 = convertintoInteger(mapQuestionScore.get("Q5.4"));
		q6_3 = convertintoInteger(mapQuestionScore.get("Q6.3"));
		q6_4 = convertintoInteger(mapQuestionScore.get("Q6.4"));
		q8_2 = convertintoInteger(mapQuestionScore.get("Q8.2"));
		ArrayList<Integer> legUsearrayList = new ArrayList<Integer>();
		legUsearrayList.add(q5_3);
		legUsearrayList.add(q5_4);
		legUsearrayList.add(q6_3);
		legUsearrayList.add(q6_4);
		legUsearrayList.add(q8_2);
		leguse = Collections.max(legUsearrayList);
		score = "";
		if (leguse == 0 || leguse == 1) {
			score = "";
		} else if (leguse == 2) {
			score = MILD;
		} else if (leguse == 3) {
			score = MODERATE;
		}
		// 11-09-2018 , Update From 4 to 4,5
		else if (leguse == 4 || leguse == 5) {
			score = SEVERE;
		}
		//15-10-2018,update from leg use to Leg use
		symptomsScoreLegUse.put("title", "Leg use");
		symptomsScoreLegUse.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q5.3");
		tmpQxCodeArray.put("Q5.4");
		tmpQxCodeArray.put("Q6.3");
		tmpQxCodeArray.put("Q6.4");
		tmpQxCodeArray.put("Q8.2");
		symptomsScoreLegUse.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreLegUse);
		JSONObject symptomsScoreMoblity = new JSONObject();
		

		int mobility = ambulation;
		
		score = "";
		if (mobility == 0 || mobility == 1) {
			score = "";
		} else if (mobility == 2) {
			score = MILD;
		} else if (mobility == 3 || mobility == 4) {
			score = MODERATE;
		} else if (mobility == 5 || mobility == 6 || mobility == 7 || mobility == 8 || mobility == 9) {
			score = SEVERE;
		}
		symptomsScoreMoblity.put("title", "Mobility");
		symptomsScoreMoblity.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q1.1");
		tmpQxCodeArray.put("Q1.2");
		tmpQxCodeArray.put("Q1.3");
		tmpQxCodeArray.put("Q1.4.1");
		tmpQxCodeArray.put("Q1.4.2");
		tmpQxCodeArray.put("Q1.4.3");
		tmpQxCodeArray.put("Q1.4.4");
		tmpQxCodeArray.put("Q2");
		symptomsScoreMoblity.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreMoblity);
		JSONObject symptomsScoreQ7 = new JSONObject();
		q7_1 = convertintoInteger(mapQuestionScore.get("Q7.1"));
		q7_2 = convertintoInteger(mapQuestionScore.get("Q7.2"));
		q7_3 = convertintoInteger(mapQuestionScore.get("Q7.3"));
		ArrayList<Integer> q7arrayList = new ArrayList<Integer>();
		q7arrayList.add(q7_1);
		q7arrayList.add(q7_2);
		q7arrayList.add(q7_3);
		int q7 = Collections.max(q7arrayList);
		score = "";
		if (q7 == 0) {
			score = "";
		} else if (q7 == 1) {
			score = MILD;
		} else if (q7 == 2) {
			score = MODERATE;
		} else if (q7 == 3 || q7 == 4) {
			score = SEVERE;
		}
		symptomsScoreQ7.put("title", "Balance");
		symptomsScoreQ7.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q7.1");
		tmpQxCodeArray.put("Q7.2");
		tmpQxCodeArray.put("Q7.3");
		symptomsScoreQ7.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ7);
		JSONObject symptomsScoreQ4 = new JSONObject();
		int tem4 = sensory;
		if (tem4 == 0 || tem4 == 1) {
			score = "";
		} else if (tem4 == 2) {
			score = MILD;
		} else if (tem4 == 3) {
			score = MODERATE;
		} else if (tem4 == 4 || tem4 == 5 || tem4 == 6) {
			score = SEVERE;
		}
		symptomsScoreQ4.put("title", "Sensory");
		symptomsScoreQ4.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q4.1");
		tmpQxCodeArray.put("Q4.2");
		tmpQxCodeArray.put("Q4.3");
		tmpQxCodeArray.put("Q4.4");
		symptomsScoreQ4.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ4);
		JSONObject symptomsScoreQ25 = new JSONObject();
		q25 = convertintoInteger(mapQuestionScore.get("Q25"));
		if (q25 == 0 || q25 == 1) {
			score = "";
		} else if (q25 == 2 || q25 == 3 || q25 == 4) {
			score = MILD;
		} else if (q25 == 5 || q25 == 6 || q25 == 7) {
			score = MODERATE;
		} else if (q25 == 8 || q25 == 9 || q25 == 10) {
			score = SEVERE;
		}
		symptomsScoreQ25.put("title", "Pain");
		symptomsScoreQ25.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q25");
		symptomsScoreQ25.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ25);
		JSONObject symptomsScoreQ16 = new JSONObject();
		score = "";
		int tem16 = bowl;
		if (tem16 == 0) {
			score = "";
		} else if (tem16 == 1) {
			score = MILD;
		} else if (tem16 == 2) {
			score = MODERATE;
		} else if (tem16 == 3 || tem16 == 4) {
			score = SEVERE;
		}
		symptomsScoreQ16.put("title", "Bowel");
		symptomsScoreQ16.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q16.1");
		tmpQxCodeArray.put("Q16.2");
		symptomsScoreQ16.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ16);

		// -- Logic For Bladder Starts
		JSONObject symptomsScoreQ17 = new JSONObject();

		score = "";
		q17_1 = convertintoInteger(mapQuestionScore.get("Q17.1"));
		q17_2 = convertintoInteger(mapQuestionScore.get("Q17.2"));
		// Solved : 12thOct2018 Added Q19
		q19 = convertintoInteger(mapQuestionScore.get("Q19"));

		ArrayList<Integer> q17arrayList = new ArrayList<Integer>();
		q17arrayList.add(q17_1);
		q17arrayList.add(q17_2);
		q17arrayList.add(q19);

		int q17 = Collections.max(q17arrayList);

		if (q17 == 0) {
			score = "";
		} else if (q17 == 1) {
			score = MILD;
		} else if (q17 == 2) {
			score = MODERATE;
		} else if (q17 == 3 || q17 == 4 || q17 == 5) {
			score = SEVERE;
		}
		symptomsScoreQ17.put("title", "Bladder");
		symptomsScoreQ17.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q17.1");
		tmpQxCodeArray.put("Q17.2");
		tmpQxCodeArray.put("Q19");
		symptomsScoreQ17.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ17);

		JSONObject symptomsScoreQ24 = new JSONObject();

		q24_1 = convertintoInteger(mapQuestionScore.get("Q24b.1"));
		q24_2 = convertintoInteger(mapQuestionScore.get("Q24b.2'"));
		q24_3 = convertintoInteger(mapQuestionScore.get("Q24b.3"));
		q24_4 = convertintoInteger(mapQuestionScore.get("Q24b.4"));

		ArrayList<Integer> q24arrayList = new ArrayList<Integer>();
		q24arrayList.add(q24_1);
		q24arrayList.add(q24_2);
		q24arrayList.add(q24_3);
		q24arrayList.add(q24_4);

		int q24b = Collections.max(q24arrayList);
		
		if (q24b == 0) {
			q24_1 = convertintoInteger(mapQuestionScore.get("Q24c.1"));
			q24_2 = convertintoInteger(mapQuestionScore.get("Q24c.2'"));
			q24_3 = convertintoInteger(mapQuestionScore.get("Q24c.3"));
			q24_4 = convertintoInteger(mapQuestionScore.get("Q24c.4"));

			q24arrayList = new ArrayList<Integer>();
			q24arrayList.add(q24_1);
			q24arrayList.add(q24_2);
			q24arrayList.add(q24_3);
			q24arrayList.add(q24_4);

			q24b = Collections.max(q24arrayList);

		}
		score = "";
		
		switch (q24b) {
		case 1:
			score = "";
			break;
		case 2:
			score = MILD;
			break;
		case 3:
			score = MODERATE;
			break;
		case 4:
			score = SEVERE;
			break;
		}
		symptomsScoreQ24.put("title", "Sexual");
		symptomsScoreQ24.put("score", score);
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q24b.1");
		tmpQxCodeArray.put("Q24b.2");
		tmpQxCodeArray.put("Q24b.3");
		tmpQxCodeArray.put("Q24b.4");
		symptomsScoreQ24.put("qx_code", tmpQxCodeArray);
		symptomsArray.put(symptomsScoreQ24);
		JSONObject symptomsScoreQ26 = new JSONObject();
		score = "";
		String q26 = mapQuestionScore.get("Q26");
		symptomsScoreQ26.put("title", "Quality of Life");
		if (q26 != null) {
			symptomsScoreQ26.put("score", q26);
		} else {
			symptomsScoreQ26.put("score", "");
		}
		tmpQxCodeArray = new JSONArray();
		tmpQxCodeArray.put("Q26");
		symptomsScoreQ26.put("qx_code", tmpQxCodeArray);
		
		symptomsArray.put(symptomsScoreQ26);
		responseJson.put("Symptoms_score", symptomsArray);
	}
	// Below Method is Responsible for EDSS Score Calculation

	public void calculateEDSS(JSONObject responseJson,Map<String, String> mapQuestionScore) {
		int fss[] = new int[8];
		int sfs = vision + brainstem + cerebellar + pyramidal + sensory + bowlandBladder + cerebral + ambulation;
		fss[0] = vision;
		fss[1] = brainstem;
		fss[2] = cerebellar;
		fss[3] = pyramidal;
		fss[4] = sensory;
		fss[5] = bowlandBladder;
		fss[6] = cerebral;
		fss[7] = ambulation;
		for (int i = 0; i < 8; i++) {
			if (fss[i] == 2) {
				fs2++;
			}
			if (fss[i] == 3) {
				fs3++;
			}
			if (fss[i] == 4) {
				fs4++;
			}
			if (fss[i] == 5) {
				fs5++;
			}
		}
		String edss = "0";
		// sfs=0
		if (sfs == 0) {
			edss = "0";
		}
		// sfs=1
		else if (sfs == 1) {
			edss = "1";
		}
		// fs2=0 and fs3=0 and fs4=0 and fs5=0 and sfs=2
		else if (fs2 == 0 && fs3 == 0 && fs4 == 0 && fs5 == 0 && sfs == 2) {
			edss = "1.5";
		}
		// fs2=0 and fs3=0 and fs4=0 and fs5=0 and sfs>2 or fs2=1 and fs3=0 and
		// fs4=0 and fs5=0
		else if ((fs2 == 0 && fs3 == 0 && fs4 == 0 && fs5 == 0 && sfs > 2)
				|| (fs2 == 1 && fs3 == 0 && fs4 == 0 && fs5 == 0)) {
			edss = "2";
		}
		// fs2=2 and fs3=0 and fs4=0 and fs5=0
		else if (fs2 == 2 && fs3 == 0 && fs4 == 0 && fs5 == 0) {
			edss = "2.5";
		}
		// fs2<=1 and fs3=1 and fs4=0 and fs5=0 or fs2>=3 and fs2<=4 and fs3=0
		// and fs4=0 and fs5=0
		else if ((fs2 <= 1 && fs3 == 1 && fs4 == 0 && fs5 == 0)
				|| (fs2 >= 3 && fs2 <= 4 && fs3 == 0 && fs4 == 0 && fs5 == 0)) {
			edss = "3";
		}
		// fs2>=2 and fs2<=3 and fs3=1 and fs4=0 and fs5=0 or fs2=0 and fs3=2
		// and fs4=0 and fs5=0 or fs2=5 and fs3=0 and fs4=0 and fs5=0 or fs2=0
		// and fs3=0 and fs4=1 and fs5=0
		else if ((fs2 >= 2 && fs2 <= 3 && fs3 == 1 && fs4 == 0 && fs5 == 0)
				|| (fs2 == 0 && fs3 == 2 && fs4 == 0 && fs5 == 0) || (fs2 == 5 && fs3 == 0 && fs4 == 0 && fs5 == 0)
				|| (fs2 == 0 && fs3 == 0 && fs4 == 1 && fs5 == 0)) {
			edss = "3.5";
		}
		// 1b=2 and 2<=2 or 1b>=3 and 1b<=4 and 2=1
		else if ((b1 == 2 && q2 <= 2) || (b1 >= 3 && b1 <= 4 && q2 == 1)) {
			edss = "4.5";
		}
		// 1b>=1 and 1b<=3 and 2>=3 or 1b>=3 and 1b<=4 and 2=2 or 1c=1 and 2<=2
		// and 1d.A>=80
		else if ((b1 >= 1 && b1 <= 3 && q2 >= 3) || (b1 >= 3 && b1 <= 4 && q2 == 2)
				|| (c1 >= 1 && q2 <= 2 && d1a >= 80)) {
			edss = "5";
		}
		// 1b>=3 and 1b<=4 and 2>=3 or 1c=1 and 2<=2 and 1d.A>=60 and 1d.A<80 or
		// 1c=1 and 2>=3 and 1d.A>=80 || 1c>=2 and 1c<=3 and (1d.C+1d.D)<=20 and
		// 1d.B<20
		else if ((b1 >= 3 && b1 <= 4 && q2 >= 3) || (c1 == 1 && q2 <= 2 && d1a >= 60 && d1a < 80)
				|| (c1 == 1 && q2 >= 3 && d1a >= 80) || (c1 >= 2 && c1 <= 3 && (d1c + d1d) <= 20 && d1b < 20)) {
			edss = "5.5";
		}
		// 1c=1 and 2>=3 and 1d.A>=60 and 1d.A<80 or 1c=1 and 1d.A<60 and
		// (1d.C+1d.D)<50 or 1c>=2 and 1c<=3 and (1d.C+1d.D)<=20 and 1d.B>=20
		else if ((c1 == 1 && q2 >= 3 && d1a >= 60 && d1a < 80) || (c1 == 1 && d1a < 60 && (d1c + d1d) < 50)
				|| (c1 >= 2 && c1 <= 3 && (d1c + d1d) <= 20 && d1b >= 20)) {
			edss = "6";
		}
		// 1c=1 and (1d.C+1d.D)>=50 1c=1 and (1d.C+1d.D)>=50 or 1c>=2 and 1c<=3
		// and (1d.C+1d.D)>20 and 1d.D<40 or 1c=2 and (1d.C+1d.D)>20 and
		// (1d.C+1d.D)<60 and 1d.D>=40 or 1c=3 and (1d.C+1d.D)>20 and
		// (1d.C+1d.D)<50 and 1d.D>=40
		else if ((c1 == 1 && (d1c + d1d) >= 50) || (c1 >= 2 && c1 <= 3 && (d1c + d1d) > 20 && d1d < 40)
				|| (c1 == 2 && (d1c + d1d) > 20 && (d1c + d1d) < 60 && d1d >= 40)
				|| (c1 == 3 && (d1c + d1d) > 20 && (d1c + d1d) < 50 && d1b >= 40)) {
			edss = "6.5";
		}
		// 1c=2 and (1d.C+1d.D)>20 and (1d.C+1d.D)>=60 and 1d.D>=40 or 1c=3 and
		// (1d.C+1d.D)>20 and (1d.C+1d.D)>=50 and 1d.D>=40 or 1a=3 and 2<=3
		else if ((c1 == 2 && (d1c + d1d) > 20 && (d1c + d1d) >= 60 && d1b >= 40)
				|| (c1 == 3 && (d1c + d1d) > 20 && (d1c + d1d) >= 50 && d1b >= 40) || (a1 == 3 && q2 <= 3)) {
			edss = "7";
		}
		// 1a=3 and 2=4 and sfs<20 or 1a=3 and 2=5 and sfs<=15
		else if ((a1 == 3 && q2 == 4 && sfs < 20) || (a1 == 3 && q2 == 5 && sfs <= 15)) {
			edss = "7.5";
		}
		// 1a=3 and 2=4 and sfs>=20 or 1a=3 and 2=5 and sfs>15 or 1a=4 and 2<5
		else if ((a1 == 3 && q2 == 4 && sfs >= 20) || (a1 == 3 && q2 == 5 && sfs > 15) || (a1 == 4 && q2 < 5)) {
			edss = "8";
		}
		// 1a=4 and 2=5 and sfs<20
		else if (a1 == 4 && q2 == 5 && sfs < 20) {
			edss = "8.5";
		}
		// 1a=4 and 2=5 and sfs>=20 and sfs<30
		else if (a1 == 4 && q2 == 5 && sfs >= 20 && sfs < 30) {
			edss = "9";
		}
		// 1a=4 and 2=5 and sfs>=30
		else if (a1 == 4 && q2 == 5 && sfs >= 30) {
			edss = "9.5";
		}
		// if none of the above apply
		else {
			edss = "4";
		}
		responseJson.put("edss_score", edss);
	}
}