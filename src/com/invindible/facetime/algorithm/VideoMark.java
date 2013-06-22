package com.invindible.facetime.algorithm;

public class VideoMark {
	public static int mark(double[][] test,double[][] model,double[] testMean,double[][] modelMean,double[] allMean){
		double value=L2Form.value(modelMean,allMean);		//l2 domain 
		double l1value=L1Form.l1value(modelMean,allMean);   //l1 domain			
		double[] madis=MixedMahalnobisDistance.calMixedMahalnobisDistance(testMean,modelMean);	 //ma distance	
		double[] mindis=MixedMahalnobisDistance.calMinDistance(testMean, modelMean);  //min distance
		int matmp=Identify.mark(Double.MAX_VALUE,madis);	//ma domain
		int mintmp=Identify.mark(Double.MAX_VALUE,mindis);  //min domain
		
		double[] facedis;  //l2 distance
		double[] l1dis;   //l1 distance
		int[] record=new int[3];
		for(int i=0;i<=test.length;i++){
			boolean l2,l1;
			int[] identify=new int [4];
			int error1=0;
			if(i==test.length){
				l2=L2Form.inL2(testMean, allMean, value);
				l1=L1Form.inL1(testMean, allMean, l1value);	//whether in or not
				if(!l1||!l2)
				{
					record[i]=-1;
					System.out.println("不在范围内");
					continue;
				}
				facedis=L2Form.L2Form(model, testMean);  //calculate l2 distance
				l1dis=L1Form.L1Form(model, testMean);   //calculate l1 distance
			}
			else
			{
				l2=L2Form.inL2(test[i], allMean, value);
				l1=L1Form.inL1(test[i], allMean, l1value);
				if(!l1||!l2)
				{
					record[i]=-1;
					System.out.println("不在范围内");
					continue;
				}
				facedis=L2Form.L2Form(model, test[i]);  //calculate l2 distance
				l1dis=L1Form.L1Form(model, test[i]);   //calculate l1 distance
			}			
			
			
			//-----------------------与平均比较
			double[] l2facedis;
			double[] l1facedis;      //l1 form
			if(i==test.length){
				l2facedis=L2Form.L2Form(modelMean, testMean);		//l2 form
				l1facedis=L1Form.L1Form(modelMean, testMean); 
			}
			else
				{
					l2facedis=L2Form.L2Form(modelMean, test[i]);
					l1facedis=L1Form.L1Form(modelMean, test[i]);
				}
			int tmp=Identify.mark(value, l2facedis);
			int l1tmp=Identify.mark(l1value, l1facedis);
			
			 if(tmp!=-1){
					int second=second(tmp,l2facedis);
					identify[0]=tmp+1;
					if(second!=-1){
					if(l2facedis[tmp]<0.55*l2facedis[second])
					{;}
					else
						error1++;					
				}
			 }
			 else
			 {
					record[i]=-1;
					System.out.println("l2均值不在范围内");
					continue;
			}
			 
			 if(l1tmp!=-1){
					int second=second(tmp,l1facedis);
					identify[1]=l1tmp+1;
					if(second!=-1){
					if(l1facedis[l1tmp]<0.55*l1facedis[second])
					{;}
					else
						error1++;					
				}
			 }
			 else
			 {
					record[i]=-1;
					System.out.println("l1均值不在范围内");
					continue;
				}
			 
			 identify[2]=matmp+1;
			 identify[3]=mintmp+1;
			 
			//-----------------------与平均比较
			 for(int c=0;c<identify.length;c++){
				 System.out.println(identify[c]);
			 }
			 
			 for(int c=0;c<identify.length;c++){
					for(int d=c+1;d<identify.length;d++){
						if(identify[c]!=identify[d])
						{
							record[i]=-1;
							System.out.println("4个值不等");
							break;
						}
					}
					if(record[i]==-1)
						{
							break;
						}
					else if(c==identify.length-1){
						record[i]=identify[c];
					}
				}
			 if(error1>=2)
				 record[i]=-1;
			 System.out.println("record "+i+" :"+record[i]);
			
		}
		if(record[0]==record[1]&&record[1]==record[2]&&record[1]!=-1)
			return record[1];
		else if((record[0]==record[2]||record[1]==record[2])&&record[2]!=-1)
			return record[2];
		else
			return -1;
	}
	
	private static int second(int min,double[] dis){
		double tmp=Double.MAX_VALUE;
		//System.out.println(tmp);
		int second=0;
		for(int i=0;i<dis.length;i++){
			//System.out.println(dis[i]);
			if(tmp>dis[i]&&i!=min)
				{
					tmp=dis[i];
					second=i;
				}
		}
		return second;
	}
}