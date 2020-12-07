package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;


public class 服务器端测试 {
    static JFrame frame = new JFrame("服务器端");
    private static JScrollPane scrollPane = null;
    private static JTextPane text = null;
    private static StyledDocument doc = null;
    
    public 服务器端测试(){
    	 text = new JTextPane();
         text.setEditable(false);
         doc = text.getStyledDocument(); // 获得JTextPane的Document
         scrollPane = new JScrollPane(text);
         scrollPane.setPreferredSize(new Dimension(500, 500));
         frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
         frame.pack();
    }

    
  
    private static Set<String> names = new HashSet<>();//保存名字到集合中
    private static Set<PrintWriter> writers = new HashSet<>();
    
    public static void main(String[] args) throws Exception {
        
         服务器端测试 server = new 服务器端测试();
        server.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.frame.setVisible(true);
        doc.insertString(doc.getLength(),  "服务器启动\n服务器运行中..........\n\n",null);
        
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {//监听59001端口
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

 
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

   
        public Handler(Socket socket) {
            this.socket = socket;
        }

        
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!(name.length()==0) && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }


                out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " 加入聊天室");
                    writer.println("MESSAGE " +"当前在线人数："+names.size());
                    writer.println("MESSAGE " +"当前在线用户："+names.toString().substring(1, names.toString().length()-1));
                }
                doc.insertString(doc.getLength(),"用户："+ name + " 加入聊天室\n",null);
                doc.insertString(doc.getLength(), "当前在线人数："+names.size()+"\n",null);
                doc.insertString(doc.getLength(), "当前在线用户："+names.toString().substring(1, names.toString().length()-1)+"\n",null);
                writers.add(out);
                
                // 接收客户端信息并向客户端发送信息
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }

                    //将客户之间的通信信息打印到服务器端窗口上
                    //使用正则表达式  若发送的信息包含图片代号 则打印图片出来
                    String str=input;
                    if(str.matches(".*#10[0-9]{2}.*")){//是否包含图片代号，有则进入匹配
                        String strs[]=str.split("#");
                        for(int i=0;i<strs.length;i++){
                            if(i==0){//第一个字符串数组不包含图片，将字符串打印到窗口，并打印上用户名字
                                try {
                            
                                    doc.insertString(doc.getLength(), "客户端 "+name+"："+strs[i] + " ",null);//去掉“MESSAGE”
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                if(strs[i].matches("10[0-9]{2}")){//只有4个数字且以10开头
                                    String path="src/images/"+strs[i]+".gif";
                                    try{
                                    text.setCaretPosition(doc.getLength()-1);//图片插入位置，文本最后
                                    text.insertIcon(new ImageIcon(path)); // 插入图片
                                    if(i==strs.length-1){//如果这个是最后一个字符串数组了，则进行换行
                                    	doc.insertString(doc.getLength()-1,  "\n\n",null);
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
                            doc.insertString(doc.getLength(), "客户端  "+name+":"+input + "\n\n",null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                    
  
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
               
                    try {//将用户离开信息打印到服务器端窗口上
						doc.insertString(doc.getLength(),"用户："+name+" 离开聊天室\n",null);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
                    names.remove(name);//将离开的用户的名字从集合里删除
           
                    try {//将当前在线人数和用户名字打印到服务器端窗口上
						doc.insertString(doc.getLength(), "当前在线人数："+names.size()+"\n",null);
						if(names.size()!=0){
							doc.insertString(doc.getLength(), "当前在线用户："+names.toString().substring(1, names.toString().length()-1)+"\n",null);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
                    for (PrintWriter writer : writers) {//向客户端发送信息：xxx离开聊天室，并显示当前在线用户和人数
                        writer.println("MESSAGE " + name + " 离开聊天室");
                        if(names.size()!=0){
                        	writer.println("MESSAGE " +"当前在线人数："+names.size());
                        	writer.println("MESSAGE " +"当前在线用户："+names.toString().substring(1, names.toString().length()-1));
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}