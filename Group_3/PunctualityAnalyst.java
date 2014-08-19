import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.NormalDist;

public class PunctualityAnalyst {

	final static int MAX_STATE = 25;
	
	ArrayList<double[]> inKs;
	ArrayList<double[]> notInKs;
	
	public PunctualityAnalyst() {

	}
	
	public double rexp(double lambda) {
		return -Math.log(Math.random())/lambda;
	}
	
	static public String floatRep(double x, int acc, boolean exp) {
		return shiftedFloatRep(x, acc, 0, exp);
	}
	
	static public String shiftedFloatRep(double x, int acc, int shift, boolean exp) {
		if(x == 0) return "0";
		int n = (int) Math.floor(Math.log(x)/Math.log(10));
		if(n >= -1) return  ""+Math.round(Math.pow(10,acc)*x)/Math.pow(10,acc);
		double z = Math.round(Math.pow(10,-n)*x*Math.pow(10,acc))/Math.pow(10,acc+shift);
		String dfs = "0."; for(int i=0;i<acc;i++) {dfs += "0";}
		DecimalFormat df = new DecimalFormat(dfs); 
		String result = df.format(z);
		if(exp) result += "\\exr{"+n+"}";
		return result;
	}
	
	public int[] processLine(String s) {
		int i=0;
		int[] result = new int[2];
		String string = "";
		for(;s.charAt(i) != ',';i++) {string += s.charAt(i);}
		result[0] = Integer.parseInt(string);
		for(;!Character.isDigit(s.charAt(i));i++) {}
		string = "";
		for(;i<s.length();i++) {string += s.charAt(i);}
		result[1] = Integer.parseInt(string);
		return result;
	}
	
	public void bootstrap(int N) {
		double[] result = new double[N];
		for(int i=0;i<N;i++) {
			double timeInK = 0;
			double timeNotInK = 0;
			for(int j=0;j<inKs.size();j++) {timeInK +=  inKs.get((int) (Math.floor(Math.random()*inKs.size())))[0];}
			for(int j=0;j<notInKs.size();j++) {timeNotInK +=  notInKs.get((int) (Math.floor(Math.random()*notInKs.size())))[0];}
			result[i] = (timeInK/(timeInK+timeNotInK));
		}
		Arrays.sort(result);
//		for(int i=0;i<N;i++) {
//			System.out.println(result[i]);
//		}
		System.out.println("\\vtwo{"+floatRep(result[(int) Math.floor(0.025*N)],3,true)+"}{"+floatRep(result[(int) Math.floor(0.975*N)],3,true)+"}");
		//System.out.println("["++", "++"]");
	}
	
	public void filterSets() {
		for(int i=inKs.size()-1;i>=0;i--) {
			if(inKs.get(i)[0] == 0) inKs.remove(i);
		}
		for(int i=notInKs.size()-1;i>=0;i--) {
			if(notInKs.get(i)[0] == 0) notInKs.remove(i);
		}
	}
	
	public void determineSamplesForSteadyStateBootstrap(ArrayList<int[]> intervals, String s, int k) {
		String headways100FilePath = s;
		File file = new File(headways100FilePath); 
		try {
			inKs = new ArrayList<double[]>();
			notInKs = new ArrayList<double[]>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gb2312"));
			String temp;
			int lastT = 0;
			int totT = 0;
			boolean inK = false; //doesn't matter what it is assigned to, assuming that the first time point isn't in k
			int z = 0;
			while((temp = br.readLine()) != null) {
				int[] point = processLine(temp);
				if(z == k && !inK) {
					inK = true;
					double[] a = new double[1];
					a[0] = totT;
					totT = 0;
					notInKs.add(a);
				} else if (z != k && inK) {
					inK = false;
					double[] a = new double[1];
					a[0] = totT;
					totT = 0;
					inKs.add(a);
				}
				boolean inIntv = false;
				for(int[] iv : intervals) {
					inIntv |= point[0] > iv[0] && point[0] < iv[1];
				}
				if(inIntv) {
					totT += point[0] - lastT;
				}
				lastT = point[0];
				z = point[1];
            }
			filterSets();
			System.out.println("inside z="+k+" times: ");
			for(double[] d: inKs) {System.out.print(d[0]+", ");}
			System.out.println("\noutside z="+k+" times: ");
			for(double[] d: notInKs) {System.out.print(d[0]+", ");}
			
			double timeInK = 0;
			double timeNotInK = 0;

			for(double[] d: inKs) {timeInK += d[0];}
			for(double[] d: notInKs) {timeNotInK += d[0];}
//			String res = "c("+inKs.get(0)[0]+", "+notInKs.get(0)[0];
//			for(int i=1;i<Math.min(inKs.size(),notInKs.size());i++) {res +=  ", "+inKs.get(i)[0]+", "+notInKs.get(i)[0];}
//			System.out.println(res+")");
			
			System.out.println("\nres: "+(timeInK/(timeInK+timeNotInK)));
			
			//PrintStream out = new PrintStream(new FileOutputStream("C:\\Users\\Gebruiker\\Dropbox\\utwente backup\\research\\programmatuur\\other tools\\hyperstar-without-mathematica\\hyperstar_data\\Route100CorrectedHeadwaysRho"+rho+".txt"));
			//out.close();
			br.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void start() {
		
		int[] intv;
		String airport100Z = System.getProperty("user.dir")+File.separator+"data"+File.separator+"Route100AirportDepZ.txt";
		//airport 100
		ArrayList<int[]> intvsAirport100 = new ArrayList<int[]>();
		intv = new int[2];
		intv[0] = 3600; intv[1] = 19593;
		intvsAirport100.add(intv);
		intv = new int[2];
		intv[0] = 77821+3600; intv[1] = 105894;
		intvsAirport100.add(intv);
		intv = new int[2];
		intv[0] = 163729+3600; intv[1] = 176668;
		intvsAirport100.add(intv);
		
		int N = 1000000;

		// Used for SSBHR in paper
		for(int cf=5;cf<=9;cf++) {determineSamplesForSteadyStateBootstrap(intvsAirport100,airport100Z,cf); bootstrap(N);}
	}
	
	public static void main(String[] args) {
		new PunctualityAnalyst().start();
	}

}
