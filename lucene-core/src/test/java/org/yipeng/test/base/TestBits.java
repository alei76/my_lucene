package org.yipeng.test.base;

import org.junit.Test;

public class TestBits {
	/*
	 * byte,short位运算时都会先转换成int
	 * 清零取数要用与，某位置一可用或
	 * 若要取反和交换，轻轻松松用异或
	 */
	
	public void testBitsOperation(){
		int a = 11;
		int b = 11;
		System.out.println("------异或 ^-------");
		//位相同则为0 , 不同则为1
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(b));
		System.out.println(parseInt2Binary(a^b));
		
		
		System.out.println("------与 &-------");
		//位都为1则为1,其他则为0
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(b));
		System.out.println(parseInt2Binary(a&b));
		
		System.out.println("------或 |-------");
		//位都为0则为0,其他则为1
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(b));
		System.out.println(parseInt2Binary(a|b));
		
		System.out.println("------取反~-------");
		//按位取反~
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(~a));
		
		System.out.println("------右移 >>-------");
		//右移，移动一位，相当于缩小2.可能发生溢出。如果为负数，高位补1，否则补0
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(a>>2));
		
		System.out.println("------无符号右移 >>> -------");
		
		//右移，移动一位，可能发生溢出.不管正负，高位补0
		int c = -10;
		System.out.println(parseInt2Binary(c));
		System.out.println(parseInt2Binary(c>>2) + "  is "+(c>>2)); //
		System.out.println(parseInt2Binary(c>>>2) +"  is "+(c>>>2));
		
		System.out.println("------左移 <<-------");
		//右移，移动一位，相当于扩大2.可能发生溢出。
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(a<<2));
	}
	
	
	/* x 0101
	 * y 0111
	 * x可以分解为0101 + 0000
	 * y可以分解为0101 + 0010
	 * (相同的相加+不同相加)/2
	 * 相同部分为:x&y
	 * 不同部分相加为:x^y
	 * 所以最终结果为((x&y+x&y)>>1)＋((x^y)>>1)
	 * 化简就是公式里的。
	 */
	public void testcalcAvgNumber(){
		int x = 5;
		int y = 7;
		System.out.println(parseInt2Binary(x));
		System.out.println(parseInt2Binary(y));
		System.out.println(parseInt2Binary(x&y));
		System.out.println(parseInt2Binary(x^y));
		System.out.println(avg(x, y));
	}
	
	@Test
	public void testDemoOfBits(){
		//判断int数是奇数,还是偶数.
		int x = 5 ;
		int y = 6 ;
		System.out.println((x&1)==1);  //为1表示奇数
		System.out.println((y&1)==0);  //为0表示偶数
		
		/*取出整型数a的第k位*/
		int z = 123456;
		System.out.println(parseInt2Binary(z));
		for(int i=31;i>=0;i--){
			System.out.print(((z>>i)&1)+",");
		}
		System.out.println();
		//将第k为清0 	 a&~(1<<k)
		//将第k为改为1	 a|(1<<k)
		
		int a = 123;
		int k = 30;
		//将int类型变量循环左移k位,即a=a<<k|a>>32-k
		System.out.println(parseInt2Binary(a));
		System.out.println(parseInt2Binary(a<<k));
		System.out.println(parseInt2Binary(a>>(32-k)));
		System.out.println(parseInt2Binary(a<<k|a>>(32-k)));
	}
	
	public static int avg(int x,int y){
		return (x&y)+((x^y)>>1);
	}
	
	
	public static String parseInt2Binary(int a){
		return Integer.toBinaryString(a);
	}
}
