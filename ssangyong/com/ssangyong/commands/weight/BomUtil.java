package com.ssangyong.commands.weight;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;

/**
 * [SR170707-024] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 10.
 * Package ID : com.ssangyong.commands.weight.BomUtil.java
 */
public class BomUtil {

	public static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static OSpec getOSpec(TCComponentItemRevision ospecRev) throws Exception{

		String ospecStr = ospecRev.getProperty("item_id") + "-" + ospecRev.getProperty("item_revision_id");
		OSpec ospec = null;

		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for( int i = 0; context != null && i < context.length; i++){
			TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
			if( ospecStr.equals(ds.getProperty("object_name"))){
				File[] files = DatasetService.getFiles(ds);
				//                files = new File[1];
				//                files[0] = new File("C:\\Users\\slobbie\\Documents\\쌍용PreBOM\\Option\\OSPEC_Version_Detail_OSI-C300-001_20150522.xls");
				ospec = OpUtil.getOSpec(files[0]);
				break;
			};
		}

		return ospec;
	}

	/**
	 * BOM Window를 생성하므로, 완전히 사용 후에는 Window Close해야함.
	 * 
	 * @param rev
	 * @param revRuleName
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMLine getBomLine(TCComponentItemRevision rev, String revRuleName) throws Exception
	{
		try
		{
			if (rev == null)
				return null;

			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) rev.getSession().getTypeComponent("BOMWindow");
			TCComponentRevisionRule latestReleasedRule = null;
			TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(rev.getSession());
			for (TCComponentRevisionRule revisionRule : allRevisionRules) {
				String ruleName = revisionRule.getProperty("object_name");
				if (ruleName.equals(revRuleName)) {
					latestReleasedRule = revisionRule;
					break;
				}
			}
			if (latestReleasedRule != null)
			{
				TCComponentBOMWindow window = winType.create(latestReleasedRule);
				window.setClearCacheOnClose(true);
				TCComponentBOMLine newTopLine = window.setWindowTopLine(null, rev, null, null);

				return newTopLine;
			}

			return null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
	
	public static String getCategory(String optionValue){

		if( optionValue == null || optionValue.length() < 4){
			return null;
		}

		if( optionValue.equals("3C61") || optionValue.equals("3WCC")){
			return "301";
		}else if( optionValue.equals("3F02") || optionValue.equals("3W02")){
			return "302";
		}else if( optionValue.equals("3D00") || optionValue.equals("3WDD")){
			return "303";
		}else if( optionValue.equals("3B16") || optionValue.equals("3W16")){
			return "304";
		}else if( optionValue.equals("3A17") || optionValue.equals("3W17")){
			return "305";
		}else if( optionValue.equals("3A51") || optionValue.equals("3W51")){
			return "321";
		}else if( optionValue.equals("3E35") || optionValue.equals("3W35")){
			return "342";
		}else if( optionValue.equals("3D01") || optionValue.equals("3W01")){
			return "344";
		}else if( optionValue.equals("3A46") || optionValue.equals("3W46")){
			return "345";
		}else if( optionValue.equals("3D25") || optionValue.equals("3W25")){
			return "346";
		}else if( optionValue.indexOf("_STD") > -1 || optionValue.indexOf("_OPT") > -1 || optionValue.equals("NONE")){
			return "TRIM";
		}else{
			return optionValue.substring(0, 3);
		}
	}
	
	public static String convertToSimpleCondition(String condition){
		ArrayList<String> foundOpValueList = new ArrayList<String>();
		Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			//          System.out.println(m.start() + " " + m.group());
			foundOpValueList.add(m.group().trim());
		}

		String conditionResult = null;
		for( String opValue : foundOpValueList){
			String con = opValue.replaceAll("\"", "");
			if( conditionResult == null){
				conditionResult = con;
			}else{
				conditionResult += " " + con;
			}
		}

		if( conditionResult == null){
			conditionResult = "";
		}

		return conditionResult;
	}
	
	/*
	 *  입력된 Item에서 releaseDate에 가장 가까운 이전 Revision을 리턴하는 함수
	 */
	public static TCComponentItemRevision getItemRevisionOnReleaseDate(TCComponentItem item, Date releaseDate) throws Exception
	{
		try
		{
			TCComponentItemRevision findRevision = null;
			Date findReleaseDate = null;

			for (TCComponentItemRevision releaseReivsion : item.getReleasedItemRevisions())
			{
				Date revReleaseDate = releaseReivsion.getDateProperty(PropertyConstant.ATTR_NAME_DATERELEASED);

				if (revReleaseDate.before(releaseDate))
				{
					if ((findReleaseDate == null) || (findReleaseDate != null && findReleaseDate.before(revReleaseDate)))
					{
						findReleaseDate = revReleaseDate;
						findRevision = releaseReivsion;
					}
				}
			}

			return findRevision;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
}
