<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h2>Hello from JSP!</h2>

<c:if test="${not empty param.name}">
	<p>Welcome ${param.name}!!!!!!!!!!!!</p>
</c:if>