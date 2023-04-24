package cn.edu.sustech.cs209.chatting.server;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class ChatServer {
    private ServerSocket serverSocket;
    private ArrayList<ChatHandler> clients = new ArrayList<>();
    private ArrayList<ChatGroup> groups = new ArrayList<>();
    public ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);
                ChatHandler handler = new ChatHandler(this, socket);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException ex) {
//            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getlist(ChatHandler from){
       String a = ""   ;
        for (ChatHandler client : clients) {
            if (client != from ) {
                a = a + client.getname() + "#";
            }
        }
        return a;
    }
    public ArrayList<ChatHandler> getallClient(){
        return clients;
    }

    public boolean isinitialized(ChatHandler from, String name){
        for (ChatHandler client : clients){
            if ((client.getname().equals(name)&& client != from)||from.getname().equals("")){
                return false;
            }
        }
        return true;
    }
    public void removeClient(ChatHandler handler) {

        clients.remove(handler);
    }

    public ChatHandler getClientByName(String name) {
        for (ChatHandler client : clients) {
            if (client.getname().equals(name)) {
                return client;
            }
        }
        return null;
    }

    public ChatGroup getGroupByName(String name) {
        for (ChatGroup group : groups) {
            if (group.getname().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public void createGroup(String groupName) {
        ChatGroup group = new ChatGroup(groupName);
        boolean flag = true;
        for (ChatGroup a :groups){
            if (a.getname().equals(groupName)){
                flag = false;
            }
        }
        if (flag){
            groups.add(group);
        }
    }

//    public void joinGroup(ChatHandler client, String groupName) {
//        ChatGroup group = getGroupByName(groupName);
//        if (group != null) {
//            group.addMember(client);
//            client.send("You have joined the group (" + groupName + ").");
//        } else {
//            client.send("The group (" + groupName + ") does not exist.");
//        }
//    }

    public static void main(String[] args) {
        int port = 12345;
        ChatServer server = new ChatServer(port);
    }
}

class ChatGroup {
    private String name;


    public ChatGroup(String name) {
        this.name = name;
    }

    public String getname() {
        return name;
    }

}
class ChatHandler extends Thread {
    private ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String name = "";
    private boolean initialized = false;


    public ChatHandler(ChatServer server, Socket socket) {
        try {
            this.server = server;
            InputStream inputStream = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            in = new BufferedReader(reader);
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            out = new PrintWriter(writer, true);
//            name = socket.getInetAddress().getHostAddress();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public String  getname() {
        return name;
    }


    public void send(ChatHandler chatHandler,String message){
        chatHandler.out.println(message);
    }
    public void run() {
        try {
            // Read the client's name from the input stream and update the name.
            while (!initialized){
                String message = in.readLine();
                if (message.startsWith("initialized:")){
                    name = message.split(":")[1];
                    boolean flag = server.isinitialized(this, name);
                    out.println(flag);

                    if (!flag) {
                        name = "";
                    }else {
                        initialized = true;
                        String a = "";
                        for (int  i = 0;i< server.getallClient().size();i++){
                            a += server.getallClient().get(i).getname() + "#";
                        }
                        System.out.println("a:" + a);
                        for (int  i = 0;i< server.getallClient().size();i++){
                            server.getallClient().get(i).out.println("/denglu/" + a);
                        }
                    }
                }else {
                    out.println("false");
                }
            }

            while (true) {
                String message = in.readLine();
                if (message ==null){
                    continue;
                }
                if (message.equals("/list/")) {
                    String members = server.getlist(this);
                    out.println("/list/" + members);

                }else if (message.startsWith("/leave/")){
                    String leave_name = message.split("/leave/")[1];
                    System.out.println("leave:"+leave_name);
                    server.removeClient(this);
                    String a = "";
                    for (int  i = 0;i< server.getallClient().size();i++){
                        a += server.getallClient().get(i).getname() + "#";
                    }
                    for (int  i = 0;i< server.getallClient().size();i++){
                        server.getallClient().get(i).out.println("/leave/" + leave_name+"phw" + a);
                    }
                }else if (message.startsWith("/txt/")){
                    System.out.println("youxiaoxile");
                    System.out.println(message);
                    String[] txt = message.split("/txt/")[1].split("phw");
                    String to = txt[0];
                    String qiehuan = "";
                    if (txt.length==1){
                        qiehuan = "\n";
                    }else {
                        qiehuan = txt[1] + "\n";
                    }
                    String ans = "/txt/"+name+"phw" + qiehuan;
                    if (server.getClientByName(to)!=null){
                        server.getClientByName(to).out.println(ans);
                    }else {
//后续处理
                        System.out.println("siliao,duifangdiaoxianl");
                        System.out.println("who?"+to);
                        String filename = "chatting-client/" +  to + "+" + name + ".txt";
                        File file = new File(filename);
                        try {
                            if (file.exists()) {
                                System.out.println("wenjiancunzai");
                                FileOutputStream fos = new FileOutputStream(file, true);
                                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                osw.append(qiehuan);
                                osw.close();
                            } else {
                                System.out.println("wenjiancunzai");
                                FileOutputStream fos = new FileOutputStream(file);
                                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                osw.write(qiehuan);
                                osw.close();
                            }
                        } catch (Exception ez) {
                            ez.printStackTrace();
                        }
                    }
//                    System.out.println(ans);
                }
                else if (message.startsWith("/group/")){
                    System.out.println("qunlaile");
                    System.out.println(message);
                    String groupname = message.split("/group/")[1];
                    server.createGroup(groupname);
                    System.out.println("name of groups:" + groupname);
                }
                else if (message.startsWith("/txt2/")){
                    String sender = message.split("phw")[1];
                    System.out.println(sender + "fa le yi ge qun liao xiao xi");
                    System.out.println(message);
                    String data = message.split("phw")[0];
                    String groupname = data.split("/txt2/")[1];
                    System.out.println(data);
                    System.out.println(groupname);
                    String[] menbers = groupname.split("#");

                    for (String member :menbers){
                        System.out.println("cheng yuan :" + member);
                        if (!member.equals(name)&&!member.equals("")){
                            System.out.print(member+"ji jiang shou dao xiao xi");
                            if (server.getClientByName(member)!=null){
                                server.getClientByName(member).out.println(message);
                            }
                        }

                    }
//                    out.flush();
                }
                if (message.equals("Dwadwadwadwad")){
                    break;
                }
//                if (message.startsWith("/create")) {
//                    // Create a new group
//                    String groupName = message.substring(8);
//                    if (groupName.length() == 0) {
//                        this.send("Invalid command format. Usage: /create [group name]");
//                    } else {
//                        server.createGroup(this, groupName);
//                    }
//                } else if (message.startsWith("/join")) {
//                    // Join an existing group
//                    String groupName = message.substring(6);
//                    if (groupName.length() == 0) {
//                        this.send("Invalid command format. Usage: /join [group name]");
//                    } else {
//                        server.joinGroup(this, groupName);
//                    }
//                } else if (message.startsWith("/leave")) {
//                    // Leave the current group
//                    String groupName = message.substring(7);
//                    if (groupName.length() == 0) {
//                        this.send("Invalid command format. Usage: /leave [group name]");
//                    } else {
//                        ChatGroup group = server.getGroupByName(groupName);
//                        if (group != null) {
//                            group.removeMember(this);
//                            this.groups.remove(groupName);
//                            this.send("You have left the group (" + groupName + ").");
//                        } else {
//                            this.send("The group (" + groupName + ") does not exist.");
//                        }
//                    }
//                } else if (message.startsWith("/groups")) {
//                    // Show a list of all the available groups
//                    String groupsList = "";
////                    for (ChatGroup group : server.groups) {
////                        groupsList += group.getname() + "(" + group.getMembersList() + "), ";
////                    }
//                    if (groupsList.length() == 0) {
//                        this.send("There are no groups available.");
//                    } else {
//                        this.send("List of all available groups: " + groupsList.substring(0, groupsList.length() - 2));
//                    }
//                } else if (message.startsWith("/members")) {
//                    // Show a list of all the group members
//                    String groupName = message.substring(9);
//                    if (groupName.length() == 0) {
//                        this.send("Invalid command format. Usage: /members [group name]");
//                    } else {
//                        ChatGroup group = server.getGroupByName(groupName);
//                        if (group != null) {
//                            this.send("The members of the group (" + groupName + ") are: " + group.getMembersList());
//                        } else {
//                            this.send("The group (" + groupName + ") does not exist.");
//                        }
//                    }
//                } else if (message.startsWith("/send")) {
//                    // Send a message to a group
//                    String[] parts = message.split(" ", 3);
//                    if (parts.length == 3) {
//                        String groupName = parts[1];
//                        ChatGroup group = server.getGroupByName(groupName);
//                        if (group != null) {
//                            group.broadcast(this, parts[2]);
//                        } else {
//                            this.send("The group (" + groupName + ") does not exist.");
//                        }
//                    } else {
//                        this.send("Invalid command format. Usage: /send [group name] [message]");
//                    }
//                } else if (message.startsWith("/private")) {
//                    // Handle private chat messages
//                    String[] parts = message.split(" ", 3);
//                    if (parts.length == 3) {
//                        ChatHandler target = server.getClientByName(parts[1]);
//                        if (target != null) {
//                            target.send("[Private message from " + this.name + "]: " + parts[2]);
//                        } else {
//                            this.send("The target client is not online.");
//                        }
//                    } else {
//                        this.send("Invalid command format. Usage: /private [client] [message]");
//                    }
//                } else {
//                    // Broadcast the public message to all clients.
//                    server.broadcast(this, message);
//                }
            }

            // Handle client disconnection.
//            server.broadcast(this, "has left the chat.");
//            for (String groupName : groups) {
//                ChatGroup group = server.getGroupByName(groupName);
//                if (group != null) {
//                    group.removeMember(this);
//                }
//            }
            server.removeClient(this);
            in.close();
            out.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}





