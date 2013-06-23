package com.invindible.facetime.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JLabel;

import com.invindible.facetime.algorithm.LDA;
import com.invindible.facetime.algorithm.VideoMark;
import com.invindible.facetime.feature.GetPcaLda;
import com.invindible.facetime.model.FaceImage;
import com.invindible.facetime.model.VideoMarkModel;
import com.invindible.facetime.service.implement.FindVideoFaceImpl;
import com.invindible.facetime.service.interfaces.FindVideoFaceInterface;
import com.invindible.facetime.task.init.HarrCascadeParserTask;
import com.invindible.facetime.task.interfaces.Context;
import com.invindible.facetime.util.Debug;
import com.invindible.facetime.util.image.ImageUtil;
import com.invindible.facetime.wavelet.Wavelet;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class FrameVideo extends JFrame implements Context {

	static FrameVideo frameVideo;
	private JPanel contentPane;
	private JTextField txtPath1;
	private ImageIcon[] imageObjectsInVideo;//视频里找到的人,人数在窗体构造函数中初始化
	private ImageIcon[] imageObjectToFind = new ImageIcon[9];//要找的对象，最多9人
	private BufferedImage[] imageObjectsInVideoBuffImg;
	private BufferedImage[]imageObjectToFindBuffImg = new BufferedImage[9];
	private ArrayList<VideoMarkModel> arrVideoMarkModel = new ArrayList<VideoMarkModel>();//存储9个对象的视频检测数据(包括原图、人脸出现的视频时间、人脸图)
	private VideoMarkModel tempVideoMarkModel = new VideoMarkModel();//临时保存一次视频人脸检测数据，以供距离对比
	
//	private ImageIcon[] 
	private BufferedImage[] objectsToFind;//9个要在视频中查找的对象
	private boolean[] isObjectsSelected;//对象是否被选中的标志
	private int objectsSelectedCount;//选中的对象的数量
	
	private static double[][] modelP;//训练样本的投影Z
	private static double[] allMean;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameVideo frame = new FrameVideo();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrameVideo() {
		
		objectsToFind = new BufferedImage[9];
		isObjectsSelected = new boolean[9];
		
		//初始化arrVideoMarkModel
		VideoMarkModel vmmTemp = new VideoMarkModel();
		vmmTemp.setMark(-1);
		double disMinMax = Double.MAX_VALUE;
		double[] dis={disMinMax,disMinMax,disMinMax,disMinMax};
		vmmTemp.setDis(dis);
		for(int i=0; i<9; i++)
		{
			arrVideoMarkModel.add(vmmTemp);
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 935, 505);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panelVideo1 = new JPanel();
		panelVideo1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u89C6\u98911", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelVideo1.setBounds(39, 37, 287, 92);
		contentPane.add(panelVideo1);
		panelVideo1.setLayout(null);
		
		JButton btnPath1 = new JButton("选择视频1");
		btnPath1.setBounds(153, 47, 115, 23);
		btnPath1.setFont(new Font("宋体", Font.PLAIN, 14));
		panelVideo1.add(btnPath1);
		
		txtPath1 = new JTextField();
		txtPath1.setBounds(54, 16, 214, 21);
		panelVideo1.add(txtPath1);
		txtPath1.setColumns(10);
		
		JLabel lblPath1 = new JLabel("路径：");
		lblPath1.setBounds(10, 19, 45, 15);
		lblPath1.setFont(new Font("宋体", Font.PLAIN, 14));
		panelVideo1.add(lblPath1);
		
		JPanel panelVideos = new JPanel();
		panelVideos.setBorder(new TitledBorder(null, "\u89C6\u9891\u9009\u62E9", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelVideos.setBounds(6, 16, 385, 356);
		contentPane.add(panelVideos);
		panelVideos.setLayout(null);
		
		JPanel panelObjects = new JPanel();
		panelObjects.setBorder(new TitledBorder(null, "JPanel title", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelObjects.setBounds(432, 17, 456, 406);
		contentPane.add(panelObjects);
		panelObjects.setLayout(null);
		
		JPanel panelObject1 = new JPanel();
		panelObject1.setBounds(6, 17, 208, 184);
		panelObjects.add(panelObject1);
		panelObject1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u5BF9\u8C611", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelObject1.setLayout(null);
		
		JLabel lblObject1 = new JLabel("暂无选择");
		lblObject1.setBounds(43, 14, 128, 128);
		panelObject1.add(lblObject1);
		
		JButton btnObject1 = new JButton("选择对象1");
		btnObject1.setFont(new Font("宋体", Font.PLAIN, 12));
		btnObject1.setBounds(3, 151, 95, 23);
		panelObject1.add(btnObject1);
		
		JButton btnCancleObject1 = new JButton("取消该对象");
		btnCancleObject1.setFont(new Font("宋体", Font.PLAIN, 12));
		btnCancleObject1.setBounds(108, 151, 95, 23);
		panelObject1.add(btnCancleObject1);
		
		JPanel panelObject2 = new JPanel();
		panelObject2.setBorder(new TitledBorder(null, "\u5BF9\u8C612", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelObject2.setLayout(null);
		panelObject2.setBounds(224, 17, 208, 184);
		panelObjects.add(panelObject2);
		
		JLabel lblObject2 = new JLabel("暂无选择");
		lblObject2.setBounds(43, 14, 128, 128);
		panelObject2.add(lblObject2);
		
		JButton btnObject2 = new JButton("选择对象2");
		btnObject2.setFont(new Font("宋体", Font.PLAIN, 12));
		btnObject2.setBounds(3, 151, 95, 23);
		panelObject2.add(btnObject2);
		
		JButton btnCalcleObject2 = new JButton("取消该对象");
		btnCalcleObject2.setFont(new Font("宋体", Font.PLAIN, 12));
		btnCalcleObject2.setBounds(103, 151, 95, 23);
		panelObject2.add(btnCalcleObject2);
		
		JPanel panelObject3 = new JPanel();
		panelObject3.setBorder(new TitledBorder(null, "\u5BF9\u8C613", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelObject3.setLayout(null);
		panelObject3.setBounds(6, 212, 208, 184);
		panelObjects.add(panelObject3);
		
		JLabel lblObject3 = new JLabel("暂无选择");
		lblObject3.setBounds(43, 14, 128, 128);
		panelObject3.add(lblObject3);
		
		JButton btnObject3 = new JButton("选择对象3");
		btnObject3.setFont(new Font("宋体", Font.PLAIN, 12));
		btnObject3.setBounds(3, 151, 95, 23);
		panelObject3.add(btnObject3);
		
		JButton btnCalcleObject3 = new JButton("取消该对象");
		btnCalcleObject3.setFont(new Font("宋体", Font.PLAIN, 12));
		btnCalcleObject3.setBounds(108, 151, 95, 23);
		panelObject3.add(btnCalcleObject3);
		
		JButton btnTrain = new JButton("训练样本特征");
		btnTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TrainSelectedObjects();
			}
		});
		btnTrain.setFont(new Font("宋体", Font.PLAIN, 14));
		btnTrain.setBounds(259, 365, 133, 25);
		panelObjects.add(btnTrain);
		
		JPanel panelPage = new JPanel();
		panelPage.setBounds(260, 213, 140, 133);
		panelObjects.add(panelPage);
		panelPage.setLayout(null);
		
		JLabel lblPageNum = new JLabel("第 1 页");
		lblPageNum.setBounds(42, 5, 56, 19);
		panelPage.add(lblPageNum);
		lblPageNum.setFont(new Font("宋体", Font.PLAIN, 16));
		
		JButton btnPageUp = new JButton("上一页");
		btnPageUp.setBounds(25, 34, 83, 28);
		panelPage.add(btnPageUp);
		btnPageUp.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JButton btnPageDown = new JButton("下一页");
		btnPageDown.setBounds(25, 82, 83, 28);
		panelPage.add(btnPageDown);
		btnPageDown.setFont(new Font("宋体", Font.PLAIN, 14));
		
		//视频查找人脸的线程
		new HarrCascadeParserTask(this).start();
//		FindVideoFaceInterface fvfi = new FindVideoFaceImpl(this, "E:\\VideoTest\\testVideo.wmv", 0);
		//------------------------------------问题，视频如何定位----------------------------------
		
		JButton btnStartFindFace = new JButton("开始在视频中查找人脸");
		btnStartFindFace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//确认询问是否开始查找人脸
				if( JOptionPane.YES_OPTION == 
				JOptionPane.showConfirmDialog(null, "确认开始查找人脸？这个过程可能会持续几分钟，系统开销较大。", "提示", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
				{
					//路径检测
//					if( 路径正确 )
					{
					
						//根据选择的路径
						//开始在视频中 查找人脸
						FindVideoFaceInterface fvfi = new FindVideoFaceImpl(FrameVideo.this, "E:\\VideoTest\\testVideo.wmv", 0);
//						FindVideoFaceInterface fvfi = new FindVideoFaceImpl(FrameVideo.this, txtPath1.getText(), 0);
						fvfi.findFace();
						
						
						//查找结束后，销毁查找线程的实例(fvfi.stop)
					
					}
//					else
//					{
//						JOptionPane.showMessageDialog(null, "视频路径选择错误！", "提示", JOptionPane.WARNING_MESSAGE	);
//					}
					
				}
			
			}
		});
		btnStartFindFace.setFont(new Font("宋体", Font.PLAIN, 14));
		btnStartFindFace.setBounds(203, 382, 183, 25);
		contentPane.add(btnStartFindFace);
		
		JButton btnReturn = new JButton("返回主界面");
		btnReturn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//要考虑线程暂停问题
				//应该给出用户提示
				frameVideo.dispose();
				
				MainUI.frameMainUI = new MainUI();
				MainUI.frameMainUI.setVisible(true);
				
			}
		});
		btnReturn.setBounds(25, 422, 110, 35);
		contentPane.add(btnReturn);
	}

	@Override
	public void onRefresh(Object... objects) {
		// TODO Auto-generated method stub
		switch ((Integer) objects[0]) {
		
		case FindVideoFaceInterface.FIND_VIDEO_FACE_SUCCESS:
			FaceImage fi = (FaceImage) objects[1];
			
			//尝试识别 截取到的所有人脸
			//若找到相应方法，则保存进结果中
			tryToDistinguish(fi);
			
			
			
//			Debug.print("现在时间：" + fi.getTime());
//			originPanel.setBufferImage(fi.getOriginImage());
//			originPanel.setSize(fi.getOriginImage().getWidth(), fi.getOriginImage().getHeight());
//			imagePanel.setLocation(fi.getOriginImage().getWidth() + 10, 0);
//			BufferedImage img = ImageUtil.getImgByRGB(fi.getFacesRgb().get(0).getRgbMat());
//			imagePanel.setBufferImage(img);
//			imagePanel.setSize(img.getWidth(), img.getHeight());
			break;
			
		case FindVideoFaceInterface.FIND_VIDEO_FACE_FINISH:
			Debug.print("完成了");
			break;
			
		case FindVideoFaceInterface.OPEN_VIDEO_FAIL:
			Debug.print("打开视频失败");
			break;
		
		case HarrCascadeParserTask.PARSER_SUCCESS:
			Debug.print("读取adaboost文件成功！");
			break;

		default:
			break;
		}
		
	}

	/**
	 * 尝试识别 截取到的所有人脸
	 * @param fi
	 */
	private void tryToDistinguish(FaceImage fi) {
		// TODO Auto-generated method stub
		
		int findFaceListSize = fi.getFacesRgb().size();
		
		//(将所有找到的人脸，分别拿去小波变换，进行识别)
		
		//将找到的所有人脸图，分别进行等比例变换 
		BufferedImage[] waveBeforeBuffImgs = new BufferedImage[findFaceListSize];
		for(int i=0; i<findFaceListSize; i++)
		{
			//获取人脸图
			waveBeforeBuffImgs[i] = ImageUtil.getImgByRGB(fi.getFacesRgb().get(i).getRgbMat());
			
			//等比例变换
			waveBeforeBuffImgs[i] = ImageUtil.imageScale(waveBeforeBuffImgs[i], 128, 128);
		}
		
		//对所有等比例变换后的人脸图，进行小波变换
		BufferedImage[] waveAfterBuffImgs = Wavelet.Wavelet(waveBeforeBuffImgs);
		
		for(int i=0; i<findFaceListSize; i++)
		{
			//找到一张人脸图以后
//			//对其进行等比例变换
//			BufferedImage waveTempBuffImg = 
//					ImageUtil.imageScale(fi.getFacesRgb().get(i), 128, 128);
//			
//			//将人脸图进行小波变换
//			waveTempBuffImg = Wavelet.Wavelet(fi.getFacesRgb().get(0));
			
			//计算这张图片的投影Z
			double[] tempZ = LDA.getInstance().calZ(waveAfterBuffImgs[i]);
			//将tempZ的维数扩大
			double[][] testZ = new double[1][tempZ.length];
			for(int j=0; j<tempZ.length; j++)
			{
				testZ[0][j] = tempZ[j]; 
			}
			
			//开始识别
			//若找不到，则vmm为空
			VideoMarkModel vmm = VideoMark.mark(testZ, modelP, allMean);
			
			//首先，判断vmm是否为空（即test测试样例，与model训练样例不匹配
			//若为空，则说明识别不匹配，直接对下一个进行识别
			if( vmm == null)
			{
				continue;
			}
			else
			{
				//根据识别结果的ID号，保存进ArrayList<VideoMarkModel>相应的位置中
				//若 新识别的距离 < 原识别结果的距离，则替换原识别结果
				if ( CompareDistance(vmm) )
				{
					JOptionPane.showMessageDialog(null, "成功找到一个人！！", "提示", JOptionPane.INFORMATION_MESSAGE);
					System.out.println("-----------------------===成功找到一个人！===------------------------------");
					int objectIndex = vmm.getMark();
					arrVideoMarkModel.get(objectIndex).setMark(vmm.getMark());
					arrVideoMarkModel.get(objectIndex).setDis(vmm.getDis());
				}
				//否则，若 ... > ...，则不替换结果
				else
				{
					return;
				}
			}
			
		}
		
	}

	
	/**
	 * 根据传进来的临时识别ID
	 * 去arrVideoMarkModel中查找第id个，并比较其距离
	 * 若 临时数据（共4个距离）中有3个距离 均小于 原数据中的3个距离，则返回true，代表可替换
	 * 否则，返回false，代表不可替换
	 */
	private boolean CompareDistance(VideoMarkModel vmm)
	{
		//临时距离 小于 原距离 的计数器([0,4])
		int lessCount = 0;
		
		//临时数据符合第objectIndex个对象
		//( vmm.getMark() 最小为1，但arrVideoMarkModel最小为0，故-1)
		System.out.println("vmm.getMark():  " + vmm.getMark());
		
		int objectIndex =vmm.getMark()-1;
		
		//获取原数据
		VideoMarkModel originalVmm = arrVideoMarkModel.get(objectIndex);
		
		//比较 临时数据 与 原数据 的4个距离
		double[] originalDis = originalVmm.getDis();//原数据的4个距离
		double[] newDis = vmm.getDis();//临时数据的4个距离
		for(int i=0; i<4; i++)
		{
			if( newDis[i] < originalDis[i])
			{
				lessCount++;
			}
		}
		
		//若有3个或3个以上距离 更小，说明临时数据结果更符合该识别对象
		//可以用临时数据替换原数据，返回true
		if( lessCount >= 3)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
//	/**
//	 * 查看指定用户是否已创立VideoMarkModel，并加入arrVideoMarkModel中的方法
//	 * @return
//	 */
//	private boolean ObjectVideoMarkModelExist(int id) {
//		// TODO Auto-generated method stub
//		
//		//若存在，则返回true
//		if( id <= arrVideoMarkModel.size() )
//		{
//			return true;
//		}
//		else
//		{
//			return false;
//		}
//	}
	
	/**
	 * 对选中的对象进行训练
	 */
	private void TrainSelectedObjects()
	{
		//-------------------------------测试阶段，尝试对2张人脸进行搜索-（这一部分应由界面给出)------------------------------
		//对2张测试图片进行赋值
		ImageIcon imgIcon0 = new ImageIcon("E:\\VideoTest\\test1.jpg");
		ImageIcon imgIcon1 = new ImageIcon("E:\\VideoTest\\test2.jpg");
		

		
		
		objectsToFind[0] = ImageUtil.ImageToBufferedImage(imgIcon0.getImage());
		objectsToFind[1] = ImageUtil.ImageToBufferedImage(imgIcon1.getImage());
		
		//等比例缩放成128*128
		objectsToFind[0] = ImageUtil.imageScale(objectsToFind[0], 128, 128);
		objectsToFind[1] = ImageUtil.imageScale(objectsToFind[1], 128, 128);
		
		isObjectsSelected[0] = true;
		isObjectsSelected[1] = true;
		
		objectsSelectedCount = 2;
		
		//-------------------------------测试阶段，尝试对2张人脸进行搜索-------------------------------
		
		//计算前，先设置GetPcaLda的每人照片数量
		GetPcaLda.setNum(1);
		
		//选中对象的初始化，对他们进行小波变换
		//由于加入“酱油”（放在第一位），故人数要+1
		BufferedImage[] waveBeforeObjectBImages = new BufferedImage[objectsSelectedCount+1];
		
		//获取酱油的1张图片
		ImageIcon imgIcon = new ImageIcon("Pictures/none/after37-1.jpg");
		waveBeforeObjectBImages[0] = 
				ImageUtil.ImageToBufferedImage(imgIcon.getImage());
		
		
		int index = 1;// 第0个 给了“酱油”
		for(int i=0; i<9; i++)
		{
			//若该对象被选中，则添加到waveObjectBImages里面
			if( isObjectsSelected[i] == true)
			{
				waveBeforeObjectBImages[index] = objectsToFind[i];
				index++;
			}
		}
		
		//对获取的选中对象的图片进行小波变换
		BufferedImage[] waveAfterObjectBImages = Wavelet.Wavelet(waveBeforeObjectBImages);
		
		//训练（将 选中对象的图片 投影到WoptT上)
		GetPcaLda.getResult(waveAfterObjectBImages);
		
		//计算 选中对象的图片的 投影Z	
		int peopleNum = objectsSelectedCount+1;//人数（+1为酱油）
		int photoNum = 1;//每人1张图
		modelP=new double[peopleNum*photoNum][peopleNum-1];
		for(int i=0;i<peopleNum;i++){
			modelP[i]=LDA.getInstance().calZ(waveAfterObjectBImages[i]);//投影
		}
		
//		//（投影Z的）总体均值
		allMean=new double[peopleNum-1];
		
		for(int i=0; i<peopleNum - 1; i++) //遍历维数[C-1]
		{
			for(int j=0; j<peopleNum; j++) //遍历人数
			{
				allMean[i] += modelP[j][i]; 
			}
			allMean[i] /= peopleNum;
		}
		
		JOptionPane.showMessageDialog(null, "样本训练完毕!", "提示", JOptionPane.INFORMATION_MESSAGE);
	
		//计算后，将GetPcaLda的每人照片数量设置回5
		GetPcaLda.setNum(5);
	}
}
