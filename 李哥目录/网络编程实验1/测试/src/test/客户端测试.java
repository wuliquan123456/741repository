package test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;


public class 客户端测试 {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    private JScrollPane scrollPane = null;
    private JTextPane text = null;
    private JTextField addText = null; // 文字输入框
    private StyledDocument doc = null;
    public 客户端测试(String serverAddress) {
        this.serverAddress = serverAddress;

        text = new JTextPane();//消息显示区
        text.setEditable(false);//将消息显示区设置为不可以编辑
        doc = text.getStyledDocument(); // 获得JTextPane的Document
        scrollPane = new JScrollPane(text);//将消息显示区设置为可滑动窗口
        scrollPane.setPreferredSize(new Dimension(500, 500));
        addText = new JTextField(10);//文本输入区
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(addText, BorderLayout.SOUTH);
        frame.pack();

        //文本输入区响应事件
        addText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(addText.getText());//取得输入框的内容并发送到服务器端
                addText.setText("");//将文本框清空
            }
        });
    }
    private String getName() {
        return JOptionPane.showInputDialog(frame, "", "请输入用户名：",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
        	Socket socket = new Socket(serverAddress, 59001);//通过ip地址和端口号连接上服务器
            in = new Scanner(socket.getInputStream());//输入流
            out = new PrintWriter(socket.getOutputStream(), true);//输出流

            while (in.hasNextLine()) {//收到信息
                String line = in.nextLine();//将收到的信息赋值给字符串line
                if (line.startsWith("SUBMITNAME")) {//服务器端发来：请求输入名字
                    out.println(getName());  //弹出窗口输入名字并返回服务器端
                } else if (line.startsWith("NAMEACCEPTED")) {//服务器端发来：名字已经收到
                    this.frame.setTitle("用户： " + line.substring(13));//将服务器端发来的名字设置成窗口名字
                    addText.setEditable(true);//将输入框设置成可编辑
                } else if (line.startsWith("MESSAGE")) {//服务端发来消息
                	//正则表达式匹配， #10xx# --》左右两个#号加10开头的四位数字 则识别为图片代号
                    String str=line;
                    if(str.matches(".*#10[0-9]{2}.*")){//是否包含图片代号，有则进入匹配
                        String strs[]=str.split("#");
                        for(int i=0;i<strs.length;i++){
                            if(i==0){//第一个字符串数组必定包含“MESSAGE+name”，必定不是图片代号
                                try {
                                    doc.insertString(doc.getLength(), strs[i].substring(8) + " ",null);//去掉“MESSAGE”：从第8个字符开始取值
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                if(strs[i].matches("10[0-9]{2}")){//只有4个数字且以10开头
                                    String path="src/images/"+strs[i]+".gif";//图片路径
                                    try{
                                    text.setCaretPosition(doc.getLength()-1);//图片插入位置，文本最后
                                    text.insertIcon(new ImageIcon(path)); // 插入图片
                                    if(i==strs.length-1){//如果这个是最后一个字符串数组了，则进行换行
                                    	doc.insertString(doc.getLength()-1,  "\n\n",null);//插入两个空白行
                                    }
                                    }catch(Exception e){
                                    	
                                    }

                                }else{//不满足4个数字
                                    try {
                                        doc.insertString(doc.getLength()-1, strs[i] + " ",null);
                                        if(i==strs.length-1){//如果这个是最后一个字符串数组了，则进行换行
                                        	doc.insertString(doc.getLength()-1,  "\n\n",null);
                                        }
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            

                        }
                    }else {//不包含图片代号，纯文字输出
                        try {
                            doc.insertString(doc.getLength(), line.substring(8) + "\n\n",null);//从第八个字符即名字开始取值
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                	
 
                    
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
    	客户端测试 client = new 客户端测试("172.18.42.99");//服务器端ip地址
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}