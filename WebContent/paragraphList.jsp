<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


    
<!DOCTYPE html>
	<html>
	<head>
		<meta charset="utf-8">
		<title>게시판</title>
		<style>
			*
			{
				margin:0;
				padding:0;
			}
			.paragraphListWrap
			{
				width:1200px;
				padding:15px;
				border : 1px solid lightgray;
				margin : 60px auto;			
			}
			.spanWrap
			{
				border : 1px solid lightgray;
			}
			.narrow{
				text-align:center;
				display :inline-block;
				width: 11%;
			}
			.medium{
				text-align:center;
				display :inline-block;
				width: 19%;
			}
			.wide{
				text-align:center;
				display :inline-block;
				width: 45%;
			}
			.pageNum{
				text-align: center;
			}
			.tagColor
			{
				color: blue;
			    background-color: lightblue;
			    border-color: blue;
			    
			    margin-right:5px;
			}
			
		</style>
	</head>
	<body>
		<div class ="paragraphListWrap">
			<h2>게시판</h2>
			<c:if test="${loginUser.id!=null}">
				<input class="writebutton" type="button" value="글쓰기" onclick="location.href='paragraphEditorWrite.do';">
			</c:if>
			<div class="spanWrap">
				<span>
					<span class="narrow">번호</span>
					<span class="wide">제목</span>
					<span class="narrow">글쓴이</span>
					<span class="medium">날짜</span>
					<span class="narrow">조회수</span>
				</span>
				<c:forEach items="${list }" var="list">
					<span>
						<span class="narrow">${list.getNum() }</span>
						<span class="wide">
							<a href="paragraphEachSelect.do?num=${list.getNum()}">[${list.getCategory()}]${list.getTitle()}</a>
							
							<c:set var="tag" value="${fn:split(list.getTag(),'★')}"></c:set>
								
							<c:if test="${fn:length(tag) <= 3}">
								<c:forEach items="${tag }" var="tags">
									<span class="tagColor">${tags }</span>
								</c:forEach>
							</c:if>
							<c:if test="${fn:length(tag) > 3}">
								<c:forEach begin="0" end="2" items="${tag }" var="tags">
									<span class="tagColor">${tags }</span>
								</c:forEach>
							</c:if>



						</span>
						<span class="narrow">${list.getId()}</span>
						<span class="medium">${list.getDatetime()}</span>
						<span class="narrow">${list.getHits()}</span>
					</span>				
				</c:forEach>
				
			</div>
			<div class="pageNum">
				<c:forEach begin="1" end="${ nOfPages}" var="i">
					<c:choose>
						<c:when test="${StartPage eq i}">
							<a>${i}(현재)</a>
						</c:when>
						<c:otherwise>
							<a href="paragraphList.do?startPage=${i}">${i}</a>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</div>
		</div>
	</body>
</html>