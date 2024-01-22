package com.symc.plm.me.sdv.service.migration.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLNodeListSortUtil {

	private boolean isAscendingOrder = true;
	private XPath xpath;
	
	public XMLNodeListSortUtil(){
		// xpath 생성
		this.xpath = XPathFactory.newInstance().newXPath();
	}
	
	private NodeList doSort(NodeList srcNodeList, boolean isAscendingOrder, String keyXPathExpression) throws Exception{

		// --------------------------------------------------------
		// Sort를위해 필요한 Sort 가능한 NodeList를 만들기위해
		// XML Document 객체를 생성한다.
		// --------------------------------------------------------
		Element rootElement = null;
		Document xmlDoc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			if(docBuilder!=null){
				xmlDoc = docBuilder.newDocument();
				if(xmlDoc!=null){
					rootElement = xmlDoc.createElement("SearchResult");
					xmlDoc.appendChild(rootElement);
				}
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		if(rootElement==null){
			throw new Exception("Failed to create a \"temporary NodeList\" for the alignment.");
		}
		
		// -------------------------------------
		// SrcNodeList를 그대로 사용하는 경우 Sort하는 과정에 Swap을위해 Parent Node를 불러올때 오류가 발생되므로
		// 아래와 같이 Argument로 받은 NodeList와 동일한 Node List를 생성한다.
		// -------------------------------------
		for (int i = 0; srcNodeList!=null && i < srcNodeList.getLength(); i++) {
			Node currentNode = srcNodeList.item(i);
			
			// 다른 XML Document에서 가져온 Node를 추가할때는 ImportNode를 이용한다.
			Node tempNode = xmlDoc.importNode(currentNode, true);
			// Import해서 생성된 Node를 rootElement에 추가한다.
			rootElement.appendChild(tempNode);
		}
		NodeList newNodeList = rootElement.getChildNodes();
		
		// -------------------------------------
		// 실제로 Node List를 위해 필요한 처리를 수행 한다.
		// -------------------------------------
		
		this.isAscendingOrder = isAscendingOrder;
		int nodeListSize = newNodeList.getLength();

		for(int sortingTurnIndex=(nodeListSize-1); sortingTurnIndex>0; sortingTurnIndex--) {
			
			for(int nodeIndex=0; nodeIndex<sortingTurnIndex; nodeIndex++) {

				Node currentNode = newNodeList.item(nodeIndex);
				Node nextNode = newNodeList.item(nodeIndex+1);
						
				if(nextNode==null || currentNode==null){
					continue;
				}

				boolean isSwapTarget = isSwapTarget(currentNode, nextNode, keyXPathExpression);
				if(isSwapTarget==true) {
					boolean deep = true;
					
					Node tempCurrentNode = currentNode.cloneNode(deep);
					Node tempNextNode = nextNode.cloneNode(deep);
					
					nextNode.getParentNode().removeChild(nextNode);
					if(tempNextNode!=null && currentNode!=null){
						currentNode.getParentNode().insertBefore(tempNextNode, currentNode);
					}
				}

			}      
			
		}
		
		// Sort된 Node List를 Return 한다.
		return newNodeList;
	}
	
	/**
	 * Node List를 Sort 하기위해 위치를 바꿔야될 대상인지 구분한 결과를 Return 한다.
	 * @param currentNode
	 * @param nextNode
	 * @param keyXPathExpression Node의 비교 Key String을 가져오는데 사용될 XPath 검색 문장
	 * @return
	 */
	private boolean isSwapTarget(Node currentNode, Node nextNode, String keyXPathExpression){
		
		String currentValueStr = null;
		String nextValueStr = null;
		
		// XPATH를 이용해 Attribute를 읽는다.
		try {
			currentValueStr = xpath.evaluate(keyXPathExpression, currentNode);
			nextValueStr = xpath.evaluate(keyXPathExpression, nextNode);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		int compareResult = 0; 
		if(currentValueStr!=null && nextValueStr!=null){
			compareResult = currentValueStr.compareToIgnoreCase(nextValueStr);
		}else if(currentValueStr==null && nextValueStr!=null){
			compareResult = 1; 
		}else if(currentValueStr!=null && nextValueStr==null){
			compareResult = -1;
		}else{
			compareResult = 0; 
		}
		
		boolean isSwaptarget = false;
		
		if(this.isAscendingOrder==true){
			if(compareResult>0){
				 isSwaptarget = true;
			}			
		}else{
			if(compareResult<0){
				 isSwaptarget = true;
			}
		}
		
		return isSwaptarget;
	}
	
	/**
	 * 주어진 NodeList의 Data중 주어진 XPath 검색결과 값을 기준으로 Sort한 결과 NodeList를 Return 한다.
	 * 
	 * @param nodeList 입력값이 되는 NodeList 객체
	 * @param isAscendingOrder 오름차순 정렬인 경우 True, 내림차순 정렬이면 False
	 * @param keyXPathExpression XPath를 이용해 XML Element의 Attribute 값이나 Text Node의 값을 구분하는데 사용될 Key값을 찾는 XPath조건 문자.
	 * @return Sort된 NodeList를 Return 한다.
	 * @throws Exception
	 */
	public static NodeList sortNodeList(NodeList nodeList, boolean isAscendingOrder, String keyXPathExpression) throws Exception{
		XMLNodeListSortUtil xmlNodeListSortUtil = new XMLNodeListSortUtil();
		return xmlNodeListSortUtil.doSort(nodeList, isAscendingOrder, keyXPathExpression);
	}

}
