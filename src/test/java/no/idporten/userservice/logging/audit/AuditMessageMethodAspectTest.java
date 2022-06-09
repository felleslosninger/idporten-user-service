package no.idporten.userservice.logging.audit;

import no.idporten.logging.audit.AuditEntry;
import no.idporten.logging.audit.AuditLogger;
import no.idporten.userservice.TestData;
import no.idporten.userservice.api.SearchRequest;
import no.idporten.userservice.api.UserResource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static no.idporten.userservice.logging.TokenMasker.TRUNCATED_POSTFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditMessageMethodAspectTest {

    @Mock
    ProceedingJoinPoint joinPoint;
    @Mock
    MethodSignature signature;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private ObjectProvider<HttpServletRequest> requestObjectProvider;
    @Mock
    private HttpServletRequest req;

    @InjectMocks
    private AuditMessageMethodAspect auditMessageMethodAspect;

    @Captor
    ArgumentCaptor<AuditEntry> auditEntryCaptor;

    @Test
    public void testAuditLogForBasicAuth() throws Throwable {

        when(requestObjectProvider.getObject()).thenReturn(req);
        String username = "user";
        String usernamePassword = Base64.getEncoder().encodeToString((username+":password").getBytes());
        when(req.getHeader("Authorization")).thenReturn("Basic " + usernamePassword);

        String reqParam1 = "SearchRequest(person_identifier=" + TestData.randomUser().getPid() + ")";
        mockMethodAndMethodParameters("searchUserLogin", reqParam1);

        Object body = auditMessageMethodAspect.auditLog(joinPoint);

        verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
        AuditEntry auditEntry = auditEntryCaptor.getValue();
        assertTrue(auditEntry.getAuditId().auditId().endsWith(AuditID.LOGIN_USER_SEARCHED.getAuditName()));
        assertEquals(username, auditEntry.getAttribute("username"));
        assertNull(auditEntry.getAttribute("access_token"));
        assertNull(auditEntry.getAttribute("resource"));
        assertEquals(reqParam1, auditEntry.getAttribute("request_body"));
    }


    @Test
    public void testAuditLogForAccessToken() throws Throwable {

        when(requestObjectProvider.getObject()).thenReturn(req);
        String accessToken = Base64.getEncoder().encodeToString("my-nice-token".getBytes());
        when(req.getHeader("Authorization")).thenReturn("Bearer " + accessToken);

        String reqParam1 = "SearchRequest(person_identifier=" + TestData.randomUser().getPid() + ")";
        mockMethodAndMethodParameters("searchUserAdmin", reqParam1);

        Object body = auditMessageMethodAspect.auditLog(joinPoint);

        verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
        AuditEntry auditEntry = auditEntryCaptor.getValue();
        assertTrue(auditEntry.getAuditId().auditId().endsWith(AuditID.ADMIN_USER_SEARCHED.getAuditName()));
        assertNull(auditEntry.getAttribute("username"));
        String accessTokenAnnonymous = (String) auditEntry.getAttribute("access_token");
        assertTrue(accessTokenAnnonymous.endsWith(TRUNCATED_POSTFIX));
        assertTrue(accessToken.contains(accessTokenAnnonymous.replace(TRUNCATED_POSTFIX, "")));
        assertNull(auditEntry.getAttribute("resource"));
        assertEquals(reqParam1, auditEntry.getAttribute("request_body"));
    }

    private void mockMethodAndMethodParameters(String methodName, String reqParam1) throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(signature);
        Object[] args = new Object[]{reqParam1};
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getMethod()).thenReturn(myMethod(methodName));
    }

    public Method myMethod(String methodName) throws NoSuchMethodException {
        return getClass().getDeclaredMethod(methodName, SearchRequest.class);
    }

    @AuditMessage(AuditID.LOGIN_USER_SEARCHED)
    public ResponseEntity<List<UserResource>> searchUserLogin(@RequestBody SearchRequest searchRequest) {
        UserResource user = new UserResource();
        user.setPersonIdentifier(searchRequest.getPersonIdentifier());
        return ResponseEntity.ok(Collections.singletonList(user));
    }
    @AuditMessage(AuditID.ADMIN_USER_SEARCHED)
    public ResponseEntity<List<UserResource>> searchUserAdmin(@RequestBody SearchRequest searchRequest) {
        UserResource user = new UserResource();
        user.setPersonIdentifier(searchRequest.getPersonIdentifier());
        return ResponseEntity.ok(Collections.singletonList(user));
    }

}


