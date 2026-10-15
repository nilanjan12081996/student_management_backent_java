package skyware.product;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.skywareapi.company.entity.CompanyEntity;
import skyware.product.skywareapi.company.repository.CompanyRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductApplicationTests {

	@Autowired
	private CompanyRepository companyRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Disabled("Disabled to prevent consuming MySQL auto-increment IDs on the local database")
	@Transactional
	void testCompanyEntityAndTable() {
		String testEmail = "test_" + System.currentTimeMillis() + "@acme.com";
		String testPhone = "8" + (System.currentTimeMillis() % 1000000000L);

		CompanyEntity company = CompanyEntity.builder()
				.name("Acme Corp")
				.email(testEmail)
				.phone(testPhone)
				.otp(null)
				.verifyotp(0)
				.domain("acme.com")
				.status(0)
				.adminApprove(0)
				.build();

		CompanyEntity saved = companyRepository.save(company);
		assertNotNull(saved.getId());

		Optional<CompanyEntity> found = companyRepository.findByEmail(testEmail);
		assertTrue(found.isPresent());
		assertEquals("Acme Corp", found.get().getName());
		assertEquals(testPhone, found.get().getPhone());
		assertEquals(0, found.get().getVerifyotp());
		assertEquals(0, found.get().getStatus());
		assertEquals(0, found.get().getAdminApprove());
		assertNotNull(found.get().getCreatedAt());
	}

	@Autowired
	private javax.sql.DataSource dataSource;

	@Test
	void testTeachersTableExistsAndWorks() throws Exception {
		try (var conn = dataSource.getConnection();
		     var stmt = conn.createStatement()) {
			var rs = stmt.executeQuery("DESCRIBE teachers");
			boolean hasName = false, hasEmail = false, hasPhone = false, hasOtp = false, hasVerifyotp = false, hasStatus = false;
			while (rs.next()) {
				String field = rs.getString("Field");
				if ("name".equalsIgnoreCase(field)) hasName = true;
				if ("email".equalsIgnoreCase(field)) hasEmail = true;
				if ("phone".equalsIgnoreCase(field)) hasPhone = true;
				if ("otp".equalsIgnoreCase(field)) hasOtp = true;
				if ("verifyotp".equalsIgnoreCase(field)) hasVerifyotp = true;
				if ("status".equalsIgnoreCase(field)) hasStatus = true;
			}
			assertTrue(hasName);
			assertTrue(hasEmail);
			assertTrue(hasPhone);
			assertTrue(hasOtp);
			assertTrue(hasVerifyotp);
			assertTrue(hasStatus);
		}
	}

	@Autowired
	private skyware.product.skywareapi.company.service.CompanyService companyService;

	@Test
	@Disabled("Disabled to prevent consuming MySQL auto-increment IDs on the local database")
	@Transactional
	void testCompanyRegistrationAndVerifyOtp() {
		String testEmail = "tech_" + System.currentTimeMillis() + "@solutions.com";
		String testPhone = "9" + (System.currentTimeMillis() % 1000000000L);

		var registerDto = skyware.product.skywareapi.company.dto.CompanyRegisterRequestDto.builder()
				.name("Tech Solutions")
				.email(testEmail)
				.phone(testPhone)
				.domain("techsolutions.com")
				.build();

		var registered = companyService.register(registerDto);
		assertNotNull(registered.getId());
		assertEquals("Tech Solutions", registered.getName());
		assertEquals(0, registered.getVerifyotp());
		assertEquals(0, registered.getStatus());

		var entityOpt = companyRepository.findByEmail(testEmail);
		assertTrue(entityOpt.isPresent());
		Integer generatedOtp = entityOpt.get().getOtp();
		assertNotNull(generatedOtp);

		// Verify OTP
		var verifyDto = skyware.product.skywareapi.company.dto.CompanyVerifyOtpRequestDto.builder()
				.email(testEmail)
				.otp(generatedOtp)
				.build();

		var verified = companyService.verifyOtp(verifyDto);
		assertEquals(1, verified.getVerifyotp());
		assertEquals(0, verified.getStatus());

		var updatedOpt = companyRepository.findByEmail(testEmail);
		assertTrue(updatedOpt.isPresent());
		assertEquals(1, updatedOpt.get().getVerifyotp());
		assertEquals(0, updatedOpt.get().getStatus());
		assertEquals(generatedOtp, updatedOpt.get().getOtp());

		// Test activation via token
		String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(testEmail.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		var activationResult1 = companyService.activateCompany(token);
		assertTrue(activationResult1.isNewlyActivated());
		assertEquals("activated successfully", activationResult1.getMessage());
		assertEquals(1, activationResult1.getCompany().getStatus());

		var activatedEntityOpt = companyRepository.findByEmail(testEmail);
		assertTrue(activatedEntityOpt.isPresent());
		assertEquals(1, activatedEntityOpt.get().getStatus());

		// Test clicking activate button again when already status 1 -> already activated
		var activationResult2 = companyService.activateCompany(token);
		assertFalse(activationResult2.isNewlyActivated());
		assertEquals("already activated", activationResult2.getMessage());
		assertEquals(1, activationResult2.getCompany().getStatus());

		// Test activateCompany via raw email directly
		var activationResultEmail = companyService.activateCompany(testEmail);
		assertFalse(activationResultEmail.isNewlyActivated());
		assertEquals("already activated", activationResultEmail.getMessage());
	}
}

