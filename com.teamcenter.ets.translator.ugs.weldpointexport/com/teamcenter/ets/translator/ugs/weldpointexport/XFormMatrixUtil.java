package com.teamcenter.ets.translator.ugs.weldpointexport;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * Co2 용접점의 경우 시작점과 종료점을 가지고 있으므로 이것을 "bl_plmxml_occ_xform" 값으로 입력하기위해
 * 필요한 Matrix 연산을 수행하는 Class임
 * Co2 용접은 길이가 있는 원통형을 하고 있으므로 원통의 지름과 길이를 변환하는 연산을 포함하고 있다.
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class XFormMatrixUtil {
	
	private double calculatedLength = 0.d;
	private boolean xAxisReflect = false;
	private boolean yAxisReflect = false;
	private boolean zAxisReflect = false;
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	public XFormMatrixUtil(){
		
	}
	
	public XFormMatrixUtil(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug){
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
	}
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	static public String getFormMatrix(double startX, double startY, double startZ, 
			double endX, double endY, double endZ,
			double orignDiameter, double orignLength, 
			double tragetDiameter, double targetLength,
			double unitScale
			){
		
		String fromMatrixStr = null;
		
		double[] startPoint = new double[]{startX, startY, startZ};
		double[] endPoint = new double[]{endX, endY, endZ};
		
		XFormMatrixUtil aXFormMatrixUtil = new XFormMatrixUtil();
		
		double[] rotationAngles = aXFormMatrixUtil.getCalculatedRotationData(startPoint, endPoint);

		// X축 기준회전
		double xRoattion = rotationAngles[0];		
		// Y축 기준회전
		double yRoattion = rotationAngles[1];
		// Z축 기준회전
		double zRoattion = rotationAngles[2];
		
		//System.out.println("xRoattion = "+xRoattion);
		//System.out.println("yRoattion = "+yRoattion);
		//System.out.println("zRoattion = "+zRoattion);
		//System.out.println("calculatedLength = "+aXFormMatrixUtil.calculatedLength);
		
		// Z축 중심으로 회전 Matrix 구하기
		Transform3D transform = new Transform3D();
		transform.rotZ(Math.toRadians( zRoattion ));

		// Y축을 중심으로 회전한 Matrix 구하기
		Transform3D transform2nd = new Transform3D();
		//System.out.println("transform2nd(Orign) = "+transform2nd.toString());
		// 계산 결과값의 회전방향을 변경한다.
		transform2nd.rotY( Math.toRadians( yRoattion * -1.0d ) );
		//System.out.println("transform2nd(Y Rotate) = "+transform2nd.toString());
		// Y Rotation이 +- 90도를 초과 하면 
		transform.mul(transform2nd);
		//System.out.println("transforma(Y Rotate) = "+transform2nd.toString());
		
		// X축 방향으로 길이를 늘이는 Matrix 생성 (Scale)
		Transform3D sctransform3d = new Transform3D();
		// 적용할 Scale 정보 설정
		double[] calculatedScales = aXFormMatrixUtil.getCalculatedScaleVector(
				orignDiameter, orignLength, 
				tragetDiameter, targetLength,
				unitScale);
		Vector3d scaleVector = new Vector3d(calculatedScales);
		sctransform3d.setScale(scaleVector);

		// 두 Matrix를 곱한다. (각 축방향에 대한 Scale 계산값을 합친다.)		
		transform.mul(sctransform3d);
		
//		// 이동할 변위 (위치지정)
//		Transform3D transform3d = new Transform3D();
//		Vector3d startPositionVector3d = new Vector3d(startX, startY, startZ);
//		//startPositionVector3d = new Vector3d(1.d, 1.d, 1.d);
//		//startPositionVector3d.scale(unitScale);
//		transform3d.setTranslation(startPositionVector3d);
//		transform.mul(transform3d);
		
		// Matrix4d로 변환한다.
		Matrix4d worldMatrix4d = null;
		double[] paramArrayOfDouble = new double[16];
		transform.get(paramArrayOfDouble);
		worldMatrix4d = new Matrix4d(paramArrayOfDouble);
		//System.out.println("worldMatrix4d = "+worldMatrix4d.toString());
		
		// World Matrix를 View Matrix로 변환 한다.
		Matrix4d viewMatrix4d = aXFormMatrixUtil.worldMatrixToViewMatrix(worldMatrix4d);
	
		
		Matrix4d reflectionMatrix = aXFormMatrixUtil.getReflectionMatrix();
		//System.out.println("reflectionMatrix = "+reflectionMatrix.toString());
		viewMatrix4d.mul(reflectionMatrix);
		
		viewMatrix4d.m30=startPoint[0];
		viewMatrix4d.m31=startPoint[1];
		viewMatrix4d.m32=startPoint[2];
		
		double resultLength = aXFormMatrixUtil.getCalculatedLength();
		//System.out.println("resultLength = "+resultLength);
		//System.out.println("viewMatrix4d = "+viewMatrix4d.toString().replaceAll(",", "").replaceAll("\\n", " ").trim());
		fromMatrixStr = viewMatrix4d.toString().trim();
		
		return fromMatrixStr;
	}
	
	private double[] getCalculatedRotationData(double[] startPoint, double[] endPoint){
		return getCalculatedRotationDataCase1(startPoint, endPoint);
	}
	
	private double[] getCalculatedRotationDataCase1(double[] startPoint, double[] endPoint){
		
		this.xAxisReflect = false;
		this.yAxisReflect = false;
		this.zAxisReflect = false;
		
		double startX = startPoint[0];
		double startY = startPoint[1];
		double startZ = startPoint[2];
		
		double endX = endPoint[0];
		double endY = endPoint[1];
		double endZ = endPoint[2];
		
		double absLengthX = Math.abs(endX - startX);
		double absLengthY = Math.abs(endY - startY);
		double absLengthZ = Math.abs(endZ - startZ);
		
		double lengthX = endX - startX;
		double lengthY = endY - startY;
		double lengthZ = endZ - startZ;
		
		//System.out.println("absLengthX = "+absLengthX);
		//System.out.println("absLengthY = "+absLengthY);
		//System.out.println("absLengthZ = "+absLengthZ);
		
		double xRotationAngle = 0.0d;
		double yRotationAngle = 0.0d;
		double zRotationAngle = 0.0d;
		
		// Z축 기준
		zRotationAngle = getAngle(absLengthX, absLengthY);
		double rotationAixDiaMeter = Math.sqrt(Math.pow(absLengthX, 2) + Math.pow(absLengthY, 2));
		yRotationAngle = getAngle(rotationAixDiaMeter, absLengthZ);

		// 주어진 두점의 거리 계산.
		Point3d tempStartP = new Point3d(startX, startY, startZ);
		Point3d tempEndP = new Point3d(endX, endY, endZ);
		double distance = tempStartP.distance(tempEndP);
		calculatedLength =  distance;
		
		// 두점의 절대값을 기준으로 Rotation을 계산 했으므로 
		// 각 축에 대한 Reflection 조건을 만들어 줘야 한다.		
		if(lengthX < 0){
			// YZ 평면에 대한 Reflection
			xAxisReflect = true;
		}
		if(lengthY < 0){
			// XZ 평면에 대한 Reflection
			yAxisReflect = true;
		}
		if(lengthZ < 0){
			// XY 평면에 대한 Reflection
			zAxisReflect = true;
		}
		
		zRotationAngle = Math.toDegrees(zRotationAngle);
		yRotationAngle = Math.toDegrees(yRotationAngle);

//		System.out.println("rotationAixDiaMeter = "+rotationAixDiaMeter);
//		System.out.println("calculatedLength = "+calculatedLength);
		
		double[] roatationData = new double[] {xRotationAngle, yRotationAngle, zRotationAngle};
		
		return roatationData;
	}
	
	private double getAngle(double width, double height){
		double angle = 0.d;
		
		angle = Math.atan2(height, width);
		
		return angle;
	}
	
	/**
	 * Reflection 조건에 대한 Matrix를 계산 해서 Return 한다. 
	 * 이 함수는 X,Y,Z 축 방향에 대한 Reflection 조건 Matrix를 Return 한다. 
	 * @return
	 */
	public Matrix4d getReflectionMatrix(){
		Matrix4d aMatrix4d = new Matrix4d();
		aMatrix4d.setIdentity();
		
		if(this.xAxisReflect==true){
			aMatrix4d.m00 = (aMatrix4d.m00 * -1.d);
		}
		if(this.yAxisReflect==true){
			aMatrix4d.m11 = (aMatrix4d.m11 * -1.d);
		}
		if(this.zAxisReflect==true){
			aMatrix4d.m22 = (aMatrix4d.m22 * -1.d);
		}
		
		return aMatrix4d;
	}
	
	/**
	 * Co2 용접 원통의 지름과 길이를 기준으로 원하는 지름과 길이의 원통이 되도록
	 * Scale을 조정하는 기능을 수행하는 함수
	 * @param orignDiameter Co2 용접임을 표현하는 기준인 원통의 지름값 (mm) 단위임 
	 * @param orignLength Co2 용접임을 표현하는 기준인 원통의 길이 (mm) 단위임 
	 * @param targetDiameter Co2 용접으로 표현될 원통의 지름으로 원하는 값 (mm) 단위임
	 * @param targetLength Co2 용접으로 표현될 원통의 길이로 원하는 값 (mm) 단위임, 0을 입력하는경우 계산된 값이 지정됨
	 * @param unitScale Structure의 단위계 변환을 위한 기준 Scale 값
	 * @return x,y,z 방향에 적용될 Scale 값을 가진 double[]을 반환한다.
	 */
	private double[] getCalculatedScaleVector(double orignDiameter, double orignLength,
			double targetDiameter, double targetLength, 
			double unitScale 
			){

		// x축 길이는 입력된 값이 0이 아닌경우 주어진 값을 길이가 되도록 하고
		// 입력된 값이 0인 경우 계산된 값을 사용한다.
		double xScale = 1;
		if(targetLength==0.0d){
			xScale = (calculatedLength/orignLength) * unitScale;
		}else{
			xScale = (targetLength/orignLength) * unitScale;
		}
		double yScale = (targetDiameter/orignDiameter) * unitScale;
		double zScale = (targetDiameter/orignDiameter) * unitScale;
		
		double[] calculatedScale = new double[] {xScale, yScale, zScale};
		
		return calculatedScale;
	}
	
	public double getCalculatedLength() {
		return calculatedLength;
	}

	public boolean isxAxisReflect() {
		return xAxisReflect;
	}

	public boolean isyAxisReflect() {
		return yAxisReflect;
	}

	public boolean iszAxisReflect() {
		return zAxisReflect;
	}

	
	/**
	 * World Matrix를 View Matrix로 변환 한다.
	 * 수학적 연산에 사용된 Matrix를 Teamcenter의 XFormMatrix에 사용되는 View Matrix로 변환한다.
	 * @param worldMatrix
	 * @return
	 */
	private Matrix4d worldMatrixToViewMatrix(Matrix4d worldMatrix ){
		Matrix4d viewMatrix = new Matrix4d();
		
		viewMatrix = worldMatrix;
		viewMatrix.transpose();
		
//		viewMatrix.m00 = worldMatrix.m00;
//		viewMatrix.m01 = worldMatrix.m10;
//		viewMatrix.m02 = worldMatrix.m20;
//		viewMatrix.m03 = worldMatrix.m30;
//		
//		viewMatrix.m10 = worldMatrix.m01;
//		viewMatrix.m11 = worldMatrix.m11;
//		viewMatrix.m12 = worldMatrix.m21;
//		viewMatrix.m13 = worldMatrix.m31;
//		
//		viewMatrix.m20 = worldMatrix.m02;
//		viewMatrix.m21 = worldMatrix.m12;
//		viewMatrix.m22 = worldMatrix.m22;
//		viewMatrix.m33 = worldMatrix.m23;
//		
//		viewMatrix.m30 = worldMatrix.m03;
//		viewMatrix.m31 = worldMatrix.m13;
//		viewMatrix.m32 = worldMatrix.m23;
//		viewMatrix.m33 = worldMatrix.m33;
		
		return viewMatrix;
	}
}
