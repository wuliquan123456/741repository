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


public class �������˲��� {
    static JFrame frame = new JFrame("��������");
    private static JScrollPane scrollPane = null;
    private static JTextPane text = null;
    private static StyledDocument doc = null;
    
    public �������˲���(){
    	 text = new JTextPane();
         text.setEditable(false);
         doc = text.getStyledDocument(); // ���JTextPane��Document
         scrollPane = new JScrollPane(text);
         scrollPane.setPreferredSize(new Dimension(500, 500));
         frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
         frame.pack();
    }

    
  
    private static Set<String> names = new HashSet<>();//�������ֵ�������
    private static Set<PrintWriter> writers = new HashSet<>();
    
    public static void main(String[] args) throws Exception {
        
         �������˲��� server = new �������˲���();
        server.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.frame.setVisible(true);
        doc.insertString(doc.getLength(),  "����������\n������������..........\n\n",null);
        
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {//����59001�˿�
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
                    writer.println("MESSAGE " + name + " ����������");
                    writer.println("MESSAGE " +"��ǰ����������"+names.size());
                    writer.println("MESSAGE " +"��ǰ�����û���"+names.toString().substring(1, names.toString().length()-1));
                }
                doc.insertString(doc.getLength(),"�û���"+ name + " ����������\n",null);
                doc.insertString(doc.getLength(), "��ǰ����������"+names.size()+"\n",null);
                doc.insertString(doc.getLength(), "��ǰ�����û���"+names.toString().substring(1, names.toString().length()-1)+"\n",null);
                writers.add(out);
                
                // ���տͻ�����Ϣ����ͻ��˷�����Ϣ
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }

                    //���ͻ�֮���ͨ����Ϣ��ӡ���������˴�����
                    //ʹ��������ʽ  �����͵���Ϣ����ͼƬ���� ���ӡͼƬ����
                    String str=input;
                    if(str.matches(".*#10[0-9]{2}.*")){//�Ƿ����ͼƬ���ţ��������ƥ��
                        String strs[]=str.split("#");
                        for(int i=0;i<strs.length;i++){
                            if(i==0){//��һ���ַ������鲻����ͼƬ�����ַ�����ӡ�����ڣ�����ӡ���û�����
                                try {
                            
                                    doc.insertString(doc.getLength(), "�ͻ��� "+name+"��"+strs[i] + " ",null);//ȥ����MESSAGE��
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                if(strs[i].matches("10[0-9]{2}")){//ֻ��4����������10��ͷ
                                    String path="src/images/"+strs[i]+".gif";
                                    try{
                                    text.setCaretPosition(doc.getLength()-1);//ͼƬ����λ�ã��ı����
                                    text.insertIcon(new ImageIcon(path)); // ����ͼƬ
                                    if(i==strs.length-1){//�����������һ���ַ��������ˣ�����л���
                                    	doc.insertString(doc.getLength()-1,  "\n\n",null);
                                    }
                                    }catch(Exception e){
                                    	
                                    }

                                }else{//������4������
                                    try {
                                        doc.insertString(doc.getLength()-1, strs[i] + " ",null);
                                        if(i==strs.length-1){//�����������һ���ַ��������ˣ�����л���
                                        	doc.insertString(doc.getLength()-1,  "\n\n",null);
                                        }
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            

                        }
                    }else {//������ͼƬ���ţ����������
                        try {
                            doc.insertString(doc.getLength(), "�ͻ���  "+name+":"+input + "\n\n",null);
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
               
                    try {//���û��뿪��Ϣ��ӡ���������˴�����
						doc.insertString(doc.getLength(),"�û���"+name+" �뿪������\n",null);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
                    names.remove(name);//���뿪���û������ִӼ�����ɾ��
           
                    try {//����ǰ�����������û����ִ�ӡ���������˴�����
						doc.insertString(doc.getLength(), "��ǰ����������"+names.size()+"\n",null);
						if(names.size()!=0){
							doc.insertString(doc.getLength(), "��ǰ�����û���"+names.toString().substring(1, names.toString().length()-1)+"\n",null);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
                    for (PrintWriter writer : writers) {//��ͻ��˷�����Ϣ��xxx�뿪�����ң�����ʾ��ǰ�����û�������
                        writer.println("MESSAGE " + name + " �뿪������");
                        if(names.size()!=0){
                        	writer.println("MESSAGE " +"��ǰ����������"+names.size());
                        	writer.println("MESSAGE " +"��ǰ�����û���"+names.toString().substring(1, names.toString().length()-1));
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