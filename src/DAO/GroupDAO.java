package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import DTO.GroupDTO;
import DTO.GroupMemberDTO;
import DTO.GroupModifierDTO;
import DTO.MemberDTO;

public class GroupDAO {

	private static GroupDAO instance = new GroupDAO();
	
	public static GroupDAO getInstance(){
		return instance;
	}
	
	public Connection getConnection(){
		Connection conn=null;
		String url="jdbc:mysql://127.0.0.1:3306/status200";
		String db_id="root";
		String db_pw="iotiot";
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn=DriverManager.getConnection(url, db_id, db_pw);
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Schedule DAO Error **********"+e);	
		}

		return conn;
	}
	public static void close(Connection conn, Statement stmt, ResultSet rs){
		try{
			if(rs!=null){
				rs.close();
			}
			if(stmt!=null){
				stmt.close();
			}
			if(conn!=null){
				conn.close();
			}
		}
		catch(Exception e){
			System.out.println("Close Error(val ==3) : "+ e);
		}
	}
	
	public static void close(Connection conn, Statement stmt){
		try{
			if(stmt!=null){
				stmt.close();
			}
			if(conn!=null){
				conn.close();
			}
		}
		catch(Exception e){
			System.out.println("Close Error(val == 2) : "+ e);
		}
	}

	// 그룹 정보를 가져옴 
	public List<GroupDTO> groupList(String userKey){
		List<GroupDTO> temp= new ArrayList<GroupDTO>();
		List<GroupDTO> list= new ArrayList<GroupDTO>();
	
		String sql="select groupnum from groupMember where id = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
	
		try
		{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userKey);
			
			System.out.println(pstmt);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				GroupDTO gDTO = new GroupDTO();
				gDTO.setGroupnum(rs.getString("groupnum"));
				temp.add(gDTO);
			}
			
			pstmt.clearParameters();
			System.out.println(temp.size());
			for(int i = 0; i<temp.size(); i++) {
				List<String> members = new ArrayList<String>();
				List<String> modifiers = new ArrayList<String>();
				GroupDTO gDTO = new GroupDTO();
				sql = "select * from groupdata where groupnum = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, temp.get(i).getGroupnum());
				rs = pstmt.executeQuery();
				
				while(rs.next()){
					gDTO.setGroupnum(rs.getString("groupnum"));
					gDTO.setGroupname(rs.getString("groupname"));
					gDTO.setGroupcolor(rs.getString("groupcolor"));
					gDTO.setSearchable(rs.getString("searchable"));
					gDTO.setMaster(rs.getString("master"));
				}
				pstmt.clearParameters();
				
				sql = "select groupmember.id, groupmember.invite, member.name from groupmember left join member on groupmember.id = member.id where groupmember.groupnum = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, temp.get(i).getGroupnum());
				rs = pstmt.executeQuery();
				
				while(rs.next()) {
					GroupMemberDTO gmDTO = new GroupMemberDTO();
					gmDTO.setId(rs.getString("id"));
					gmDTO.setName(rs.getString("name"));
					gmDTO.setInvite(rs.getString("invite"));

					if(gmDTO.getInvite().equals("accept")) {
						String tempstr = gmDTO.getName() + "("+gmDTO.getId()+")";
						members.add(tempstr);
					}
				}
				pstmt.clearParameters();
				
				sql = "select groupmodifier.id, member.name from groupmodifier left join member on groupmodifier.id = member.id where groupnum = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, temp.get(i).getGroupnum());
				rs = pstmt.executeQuery();

				while(rs.next()) {
					GroupModifierDTO gdDTO = new GroupModifierDTO();
					gdDTO.setId(rs.getString("id"));
					gdDTO.setName(rs.getString("name"));
					String tempstr = gdDTO.getName() + "("+gdDTO.getId()+")";
					System.out.println(tempstr);
					modifiers.add(tempstr);	
				}

				pstmt.clearParameters();
				gDTO.setMembers(members.toArray(new String[members.size()]));
				gDTO.setModifiers(modifiers.toArray(new String[modifiers.size()]));
				list.add(gDTO);
			}
		}
		catch(Exception e){
			System.out.println("Group DAO> ::Select Error(val == 1) : "+ e);
		}
		finally{
			close(conn, pstmt, rs);
		}
		return list;
	}
	
	public void groupInsert(GroupDTO gDTO){
		
		String sql="insert into groupData (groupname, groupcolor, searchable, master) values(?,?,?,?)";
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try{
			conn=getConnection();
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, gDTO.getGroupname());
			pstmt.setString(2, gDTO.getGroupcolor());
			pstmt.setString(3, gDTO.getSearchable());
			pstmt.setString(4, gDTO.getMaster());

			System.out.println(pstmt);
			pstmt.executeUpdate();
			pstmt.clearParameters();
			
			sql = "select groupnum from groupData where master = ? order by groupnum asc";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, gDTO.getMaster());
			System.out.println(pstmt);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				gDTO.setGroupnum(rs.getString("groupnum"));
			}
			pstmt.clearParameters();
		
			sql = "insert into groupMember(groupnum, id, invite) values(?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, gDTO.getGroupnum());
			pstmt.setString(2, gDTO.getMaster());
			pstmt.setString(3, "accept");
			System.out.println(pstmt);
			pstmt.executeUpdate();
			pstmt.clearParameters();
			
			sql = "insert into groupModifier(groupnum, id) values (?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, gDTO.getGroupnum());
			pstmt.setString(2, gDTO.getMaster());
			System.out.println(pstmt);
			pstmt.executeUpdate();
			pstmt.clearParameters();
		}
		catch(Exception e)
		{
			System.out.println("GroupDAO> groupData Insert Error: "+e);
		}
		finally
		{
			close(conn, pstmt);
		}
	}
	// 그룹 삭제 
	public void groupDelete(GroupDTO gDTO)
	{
		String sql ="delete from data, member, modifier using groupdata data innerjoin groupmember member innerjoin groupmodifier modifier where data.groupnum = member.groupnum and member.groupnum = modifier.groupnum and data.groupnum = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		// 그룹 삭제에 대한 알고리즘은 서블렛에 구현 
		
		try
		{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gDTO.getGroupnum());
			System.out.println(pstmt);
			pstmt.executeUpdate();
			
		}
		catch(Exception e){
			System.out.println("ScheduleDAO> groupData Delete Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	public void groupUpdate(GroupDTO gDTO){
		String sql="update groupData set groupname=?, groupcolor=?, searchable=? where groupnum=?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, gDTO.getGroupname());
			pstmt.setString(2, gDTO.getGroupcolor());
			pstmt.setString(3, gDTO.getSearchable());
			pstmt.setString(4, gDTO.getGroupnum());
			
			pstmt.executeUpdate();
		}
		catch(Exception e){
			System.out.println("ScheduleDAO > groupData UPDATE Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	public void insertModifier(GroupModifierDTO gdDTO){

		String sql="insert into groupmodifier(groupnum, id, name) value (?,?,?)";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			conn = getConnection();
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gdDTO.getGroupnum());
			pstmt.setString(2, gdDTO.getId());
			System.out.println(pstmt);
			pstmt.executeUpdate();
		}
		catch(Exception e){
			System.out.println("ScheduleDAO > groupData UPDATE Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	public void deleteModifier(GroupModifierDTO gdDTO){

		String sql="select num, id, groupnum form groupmodifier where id = ? and groupnum = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gdDTO.getId());
			pstmt.setString(2, gdDTO.getGroupnum());
			System.out.println(pstmt);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				gdDTO.setNum(rs.getString("num"));
			}
			pstmt.clearParameters();
			
			sql="delete form groupmodifier where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gdDTO.getNum());
			System.out.println(pstmt);
			pstmt.executeUpdate();
		}
		catch(Exception e){
			System.out.println("ScheduleDAO > groupData UPDATE Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	public void insertMember(GroupMemberDTO gmDTO){

		String sql="insert into groupmember(groupnum, id) value (?,?,?)";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
	
			pstmt.setString(1, gmDTO.getGroupnum());
			pstmt.setString(2, gmDTO.getId());
			pstmt.executeUpdate();
		}
		catch(Exception e){
			System.out.println("ScheduleDAO > groupData UPDATE Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	public void deleteMember(GroupMemberDTO gmDTO){

		String sql="select num, id, groupmember form groupmodifier where id = ? and groupnum = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gmDTO.getId());
			pstmt.setString(2, gmDTO.getGroupnum());
			System.out.println(pstmt);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				gmDTO.setNum(rs.getString("num"));
			}
			pstmt.clearParameters();
			
			sql="delete form groupmember where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, gmDTO.getNum());
			System.out.println(pstmt);
			pstmt.executeUpdate();
		}
		catch(Exception e){
			System.out.println("ScheduleDAO > groupData UPDATE Error : "+ e);
		}
		finally{
			close(conn, pstmt);
		}
	}
	
	// 멤버 정보를 리스트로 뽑아옴 
	// 멤버 초대 기능에 사용함 
	public List<MemberDTO> memberList(String word){
		
		String searchword = "%"+word+"%";
		List<MemberDTO> list = new ArrayList<MemberDTO>();
		
		String sql="select * from member where id like ? or name like ? limit 10";
	
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		
		try
		{
			conn=getConnection();
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, searchword);
			pstmt.setString(2, searchword);
			
			System.out.println(pstmt);
			rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				MemberDTO mDTO = new MemberDTO();
			
				mDTO.setNum(rs.getInt("num"));
				mDTO.setId(rs.getString("id"));
				mDTO.setName(rs.getString("name"));
				mDTO.setEmail(rs.getString("email"));
				mDTO.setAuthority(rs.getString("authority"));
				
				list.add(mDTO);	
			}
		}
		catch(Exception e)
		{
			System.out.println("ScheduleDAO > MemberSearch (val == 2) ERROR "+e);
		}
		finally
		{
			close(conn, pstmt, rs);
		}
		
		return list;
	}
}

/* create table groupData(groupnum int primary key auto_increment, groupname text, groupcolor text, searchable varchar(11) default 'disable', master text);
 * create table schedule(num int primary key auto_increment, title text, content text, start text, end text, color text, writer text, groupnum text);
 * create table groupMember(num int primary key auto_increment, groupnum int, id text, invite varchar(11) default 'notaccept');
 * create table groupModifier(num int primary key auto_increment, groupnum int, id text);
 */

/* insert into groupmember(groupnum, id, invite) value ('', '', 'accept');
 * insert into groupmodifier(groupnum, id) value ('', '');
 * 
 * 
 * 
 * 
 */
