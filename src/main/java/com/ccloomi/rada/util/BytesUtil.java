package com.ccloomi.rada.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

import org.xerial.snappy.Snappy;

/**© 2015-2016 CCLooMi.Inc Copyright
 * 类    名：BytesUtil
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2016年6月25日-上午11:39:12
 */
public class BytesUtil {
	private static final char[][] csm = new char[256][];
	private static final char[]csm2=new char[256];
	private static final int start=0x0100;
	static {
		String cs = "0123456789abcdef";
		int i,j,n = 0;
		for (i = 0; i < 16; i++) {
			for (j = 0; j < 16; j++) {
				csm[n++] = new char[] {cs.charAt(i),cs.charAt(j)};
			}
		}
		for(i=start,j=0;i<start+256;i++) {
			csm2[j++]|=i;
		}
	}

	public static String bytesToCCString(byte[]bytes) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<bytes.length;i++) {
			sb.append(csm2[bytes[i]&0xff]);
		}
		return sb.toString();
	}
	public static byte[] ccStringToBytes(String cc) {
		byte[]b=new byte[cc.length()];
		for(int i=0;i<b.length;i++) {
			b[i]|=(cc.charAt(i)-start);
		}
		return b;
	}
	public static String bytesToHexString(byte[] byts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < byts.length; i++) {
			sb.append(csm[byts[i]&0xff]);
		}
		return sb.toString();
	}
	public static byte[] hexStringToBytes(String hex){
		int l=(hex.length()+1)/2;
		byte[]bytes=new byte[l];
		for(int i=0,j=0;i<hex.length();i++) {
			if(i%2==0) {
				switch (hex.charAt(i)) {
				case '1':bytes[j]|=0x10;break;
				case '2':bytes[j]|=0x20;break;
				case '3':bytes[j]|=0x30;break;
				case '4':bytes[j]|=0x40;break;
				case '5':bytes[j]|=0x50;break;
				case '6':bytes[j]|=0x60;break;
				case '7':bytes[j]|=0x70;break;
				case '8':bytes[j]|=0x80;break;
				case '9':bytes[j]|=0x90;break;
				case 'a':case 'A':bytes[j]|=0xA0;break;
				case 'b':case 'B':bytes[j]|=0xB0;break;
				case 'c':case 'C':bytes[j]|=0xC0;break;
				case 'd':case 'D':bytes[j]|=0xD0;break;
				case 'e':case 'E':bytes[j]|=0xE0;break;
				case 'f':case 'F':bytes[j]|=0xF0;break;
				default:break;
				}
			}else {
				switch (hex.charAt(i)) {
				case '1':bytes[j++]|=0x01;break;
				case '2':bytes[j++]|=0x02;break;
				case '3':bytes[j++]|=0x03;break;
				case '4':bytes[j++]|=0x04;break;
				case '5':bytes[j++]|=0x05;break;
				case '6':bytes[j++]|=0x06;break;
				case '7':bytes[j++]|=0x07;break;
				case '8':bytes[j++]|=0x08;break;
				case '9':bytes[j++]|=0x09;break;
				case 'a':case 'A':bytes[j++]|=0x0A;break;
				case 'b':case 'B':bytes[j++]|=0x0B;break;
				case 'c':case 'C':bytes[j++]|=0x0C;break;
				case 'd':case 'D':bytes[j++]|=0x0D;break;
				case 'e':case 'E':bytes[j++]|=0x0E;break;
				case 'f':case 'F':bytes[j++]|=0x0F;break;
				default:j++;break;
				}
			}
		}
		return bytes;
	}
	/**
	 * 描述：字节数组转整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:26:02
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static int readBytesToInt(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=(bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=(bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:25:22
	 * @param a 整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] intToBytes(int a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>(8*(length-1-i))&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转长整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:31:49
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static long readBytesToLong(byte[]bytes,int endianness){
		long a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：长整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:32:18
	 * @param a 长整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] longToBytes(long a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>((length-1-i)*8)&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转双精度类型
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:40:16
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static double readBytesToDouble(byte[]bytes,int endianness){
		return Double.longBitsToDouble(readBytesToLong(bytes,endianness));
	}
	/**
	 * 描述：双精度类型转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:42:16
	 * @param a 双精度类型
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] doubleToBytes(double a,int length,int endianness){
		return longToBytes(Double.doubleToLongBits(a), length,endianness);
	}
	/**
	 * 描述：字节数组转Float
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:30:53
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static float readBytesToFloat(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return Float.intBitsToFloat(a);
	}
	/**
	 * 描述：Float转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:31:24
	 * @param a Float
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] floatToBytes(float a,int length,int endianness){
		long b=Float.floatToIntBits(a);
		return longToBytes(b, length,endianness);
	}

	public final static byte[]writeValueAsBytes(Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	public final static void writeValueAsBytes(Object obj,byte[] b, int off,int len){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			System.arraycopy(bos.toByteArray(), 0, b, off, len);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public final static <E>E readBytesAsObject(byte[]buf){
		if(buf!=null){
			return readBytesAsObject(buf, 0, buf.length);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public final static <E>E readBytesAsObject(byte buf[], int offset, int length){
		ByteArrayInputStream byteInStream=new ByteArrayInputStream(buf, offset, length);
		ObjectInputStream ois=null;
		try{
			ois=new ObjectInputStream(byteInStream);
			return (E) ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public final static byte[]writeValueAsBytesWithCompress(Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return Snappy.compress(bos.toByteArray());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	public final static void writeValueAsBytesWithCompress(Object obj,byte[] b, int off,int len){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			System.arraycopy(Snappy.compress(bos.toByteArray()), 0, b, off, len);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public final static <E>E readBytesAsObjectWithCompress(byte[]buf){
		if(buf!=null){
			return readBytesAsObjectWithCompress(buf, 0, buf.length);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final static <E>E readBytesAsObjectWithCompress(byte buf[], int offset, int length){
		ByteArrayInputStream byteInStream=null;
		ObjectInputStream ois=null;
		try{
	        byte[] result = new byte[Snappy.uncompressedLength(buf,offset,length)];
	        Snappy.uncompress(buf, offset, length, result, 0);
			byteInStream=new ByteArrayInputStream(result);
			ois=new ObjectInputStream(byteInStream);
			return (E) ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String digestAsCCString(byte[]b) {
		MessageDigest sha256;
		try {
			sha256 = MessageDigest.getInstance("SHA-256");
			sha256.update(b);
			return bytesToCCString(sha256.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public static String digestAsHexString(byte[]b) {
		MessageDigest sha256;
		try {
			sha256 = MessageDigest.getInstance("SHA-256");
			sha256.update(b);
			return bytesToHexString(sha256.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
