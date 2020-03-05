package com.snowk.mcdm.command.algorithm;

/* DBSCAN�㷨javaʵ�֣������ܶȵľ���
 * Դ��ο��� outsider0007
 * Reference: https://blog.csdn.net/qq_37667364/article/details/89683499*/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
 
public class DBSCAN {
	
    private static int ATRIBUTE_NUMBER = 3; // x y z
    //����뾶
    private double epsilon = 0.3;
    //��������С��������
    private int minimumNumberOfClusterMembers = 4;
    //���ݼ�
    private ArrayList<double[]> inputValues = null;
    
    public static void main(CommandSender sender, ArrayList<double[]> arrayXYZ, List<Location> locationList, String task) {
        DBSCAN one = new DBSCAN();

        one.inputValues = arrayXYZ;
        boolean doNormalize = true;

		sender.sendMessage("��b��ʼִ�С�a��l DBSCAN ��b�����㷨... ��d(" + task + ")");

        int[] labels = one.performClustering(sender, doNormalize);
        printResult(sender, labels, locationList);
    }
    
    /**
     * �������ݵı�ǩ��ֱ�ӷ��������Ļ���
     */
    public int[] performClustering(CommandSender sender, boolean doNormalize) {
    	sender.sendMessage("��a��������: ��f��l" + inputValues.size());
    	//�Ƿ����ݹ�һ�����Խ��Ӱ��Ƚϴ�
    	if(doNormalize)
    		dataNormalize();
    	int k = 0;//�ؼ���
    	List<Integer> unvisitedIndices = new ArrayList<>();
    	for(int i = 0; i < inputValues.size(); i++) {
    		unvisitedIndices.add(i);
    	}
    	//���������������Ϣ 
    	// -1��������0:δ�����ʣ�1,2,3,4...���1��2��3��4...
    	int[] labels = new int[inputValues.size()];
    	int iter = 0;
    	while(unvisitedIndices.size() != 0) {
    		//0 ���ѡ��һ��û�����ʹ���������Ϊ��ʼ
    		int index = (int) (Math.round(Math.random()*(unvisitedIndices.size()-1)));
    		index = unvisitedIndices.get(index);
    		//1��ȡ����������������
    		List<Integer> indices = coreObjectTest(index);
    		//�ж��Ƿ�������Ķ���
    		if(indices.size() >= minimumNumberOfClusterMembers) {
    			//�����ɴؽ�����չ
    			k++;
    			//���Ķ�����У�Ҳ������Ҫ����չ�ĺ��Ķ���
    			Map<Integer, List<Integer>> omega = new HashMap<>();
    			omega.put(index, indices);
    			//ֱ�����Ķ������Ϊ�յ���ֹͣ
    			do {
    				//�Ӻ��Ķ����б���ѡ��һ��
    				int index_c = omega.entrySet().iterator().next().getKey();
    				labels[index_c] = k;
    				unvisitedIndices.remove(new Integer(index_c));
    				//��ȡ����
    				List<Integer> neighborIndices = omega.get(index_c);
    				//��Omega���Ƴ�
    				omega.remove(index_c);
        			//��������
        			for(int index_ : neighborIndices) {
        				//δ�����ʻ���֮ǰ������
        				if(labels[index_] < 1) {
        					labels[index_] = k;
        					unvisitedIndices.remove(new Integer(index_));
        					//�����Ƿ��Ǻ��Ķ���,����Ǽ��뵽������
            				List<Integer> index_OfNeighborIndices = coreObjectTest(index_);
            				if(index_OfNeighborIndices.size() >= minimumNumberOfClusterMembers)
            					omega.put(index_, index_OfNeighborIndices);
        				}
        			}
    			}
    			while(omega.size() != 0);
    		} else {
    			//����
    			labels[index] = -1;
    			unvisitedIndices.remove(new Integer(index));
    		}
    		iter++;
    	}
    	sender.sendMessage("��e��������:��f��l " + iter);
    	sender.sendMessage("=================================================");
		return labels;
    }
    
    public static void printResult(CommandSender sender, int[] labels, List<Location> locationList) {
    	// -1������0,δ�����ʣ�������֣���>=1���
    	Map<Integer, ArrayList<Integer>> counts = new HashMap<>();
    	int c = 0;
    	for(int label : labels) {
    		ArrayList<Integer> indices = counts.get(label);
    		if(indices == null) {
    			ArrayList<Integer> list = new ArrayList<>();
    			list.add(c);
    			counts.put(label, list);
    		} else {
    			indices.add(c);
    		}
    		c++;
    	}
//    	sender.sendMessage("��e�� ��a-1 ��eΪ�����");
    	counts.forEach((k,v)->{
    		DecimalFormat dfInt = new DecimalFormat("#");
    		DecimalFormat df = new DecimalFormat("#.00");
    		String vsizeDF = df.format(v.size()*1.0/labels.length*100);
    		if (k != -1) {
        		double avgX = 0; double avgY = 0; double avgZ = 0;
            	String world = locationList.get(0).getWorld().getName();
            	sender.sendMessage("��b��l������: ��e�ڡ�e��l"+ k +"��e���a - ռ�������: ��c��l" + vsizeDF + "% ��e����ͳ��: ��f" + v.size() + "/" + labels.length);
            	for (int i : v) {              	// ArrayList<Integer> v // INDEX of LocationList
            		avgX += locationList.get(i).getX();
            		avgY += locationList.get(i).getY();
            		avgZ += locationList.get(i).getZ();
            	}
            	avgX /= v.size(); avgY /= v.size(); avgZ /= v.size();
            	sender.sendMessage("        - ��c��l�ܼ����ġ�b world-x-y-z: ��a��l[��e��l" + world + "��a��l, " + dfInt.format(avgX) + ", " + dfInt.format(avgY) +", " + dfInt.format(avgZ) + "]");
        	} else {
        		sender.sendMessage("��b��l������: ��e������a - ռ�������: ��c��l" + vsizeDF + "% ��e����ͳ��: ��f" + v.size() + "/" + labels.length);
        	}
    	});
    	sender.sendMessage("=================================================");
    }
    
    /**
     * ��ȡһ���������������
     * @param testSampleIndex
     * @return ������Щ������index
     */
    public List<Integer> coreObjectTest(int testSampleIndex){
    	List<Integer> indices = new ArrayList<>();
    	for(int i = 0; i < inputValues.size(); i++) {
    		if(distance(inputValues.get(testSampleIndex), inputValues.get(i)) <= epsilon) {
    			indices.add(i);
    		}
    	}
    	return indices;
    }
    /**
     * ���ݹ�һ��
     * ���������һ�����Ҳ��޸�weka��DBSCAN��������ô�������һ��
     * x = (x - min)/(max - min)
     */
    public void dataNormalize() {
    	//x = (x - min)/(max - min)
    	double[] mins = new double[ATRIBUTE_NUMBER];
    	double[] maxs = new double[ATRIBUTE_NUMBER];
    	for(int i = 0; i < ATRIBUTE_NUMBER;i++) {
    		mins[i] = Double.MAX_VALUE;
    		maxs[i] = Double.MIN_VALUE;
    	}
    	for(int i = 0; i < ATRIBUTE_NUMBER; i++) {
    		for(int j = 0; j < inputValues.size();j++) {
    			mins[i] = inputValues.get(j)[i] < mins[i] ? inputValues.get(j)[i] : mins[i];
    			maxs[i] = inputValues.get(j)[i] > maxs[i] ? inputValues.get(j)[i] : maxs[i];
    		}
    	}
    	double[] maxsReduceMins = new double[ATRIBUTE_NUMBER];
    	for(int i = 0; i < ATRIBUTE_NUMBER;i++) {
    		maxsReduceMins[i] = maxs[i] - mins[i];
    	}
    	for(int i = 0; i < ATRIBUTE_NUMBER; i++) {
    		for(int j = 0; j < inputValues.size();j++) {
    			inputValues.get(j)[i] = (inputValues.get(j)[i] - mins[i])/(maxsReduceMins[i]);
    		}
    	}
    }
    /**
     * ŷʽ����
     * @param v1
     * @param v2
     * @return
     */
    public double distance(double[] v1, double[] v2) {
		double sum = 0;
		for(int i = 0; i < v1.length; i++) {
			sum += Math.pow(v1[i]-v2[i], 2);
		}
		return Math.sqrt(sum);
	}
}