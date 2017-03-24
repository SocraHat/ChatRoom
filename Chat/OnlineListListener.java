package ChatRoom_add_4_3;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;

public class OnlineListListener implements MouseListener{
	ChatRoom_add_4_3 chat = null;
	String id = "";
	JList list = null;
	String targetID = "";
	
	OnlineListListener(ChatRoom_add_4_3 chat,JList list){
		this.chat = chat;
		this.list = list;
	}
	
	public String getTargetID(){//添加的目标好友
		return targetID;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1 ){
			
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
}
