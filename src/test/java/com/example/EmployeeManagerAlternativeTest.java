package com.example;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class EmployeeManagerAlternativeTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private BankService bankService;

	@InjectMocks
	private EmployeeManager employeeManager;



	@Spy
	private Employee notToBePaid = new Employee("1", 1000);

	@Spy
	private Employee toBePaid = new Employee("2", 2000);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testPayEmployeesWhenNoEmployeesArePresent() {
		when(employeeRepository.findAll())
			.thenReturn(emptyList());
		assertThat(employeeManager.payEmployees()).isZero();
	}

	@Test
	public void testPayEmployeesWhenOneEmployeeIsPresent() {
		when(employeeRepository.findAll())
			.thenReturn(asList(new Employee("1", 1000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		verify(bankService).pay("1", 1000);
	}

	@Test
	public void testPayEmployeesWhenSeveralEmployeeArePresent() {
		when(employeeRepository.findAll())
			.thenReturn(asList(
					new Employee("1", 1000),
					new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		verify(bankService).pay("2", 2000);
		verify(bankService).pay("1", 1000);
		verifyNoMoreInteractions(bankService);
	}

	@Test
	public void testPayEmployeesInOrderWhenSeveralEmployeeArePresent() {
		// an example of invocation order verification
		when(employeeRepository.findAll())
			.thenReturn(asList(
					new Employee("1", 1000),
					new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		InOrder inOrder = inOrder(bankService);
		inOrder.verify(bankService).pay("1", 1000);
		inOrder.verify(bankService).pay("2", 2000);
		verify(bankService,times(1)).pay("1",1000);
		verifyNoMoreInteractions(bankService);
	}

	@Test
	public void testExampleOfInOrderWithTwoMocks() {
		// Just an example of invocation order verification on several mocks
		when(employeeRepository.findAll())
			.thenReturn(asList(
					new Employee("1", 1000),
					new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		InOrder inOrder = inOrder(bankService, employeeRepository);
		inOrder.verify(employeeRepository).findAll();
		inOrder.verify(bankService).pay("1", 1000);
		inOrder.verify(bankService).pay("2", 2000);
		verifyNoMoreInteractions(bankService);
	}



	@Test
	public void testEmployeeSetPaidIsCalledAfterPaying() {
		when(employeeRepository.findAll())
			.thenReturn(asList(toBePaid));
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		InOrder inOrder = inOrder(bankService, toBePaid);
		inOrder.verify(bankService).pay("2", 2000);
		inOrder.verify(toBePaid).setPaid(true);
	}

	@Test
	public void testPayEmployeesWhenBankServiceThrowsException() {
		when(employeeRepository.findAll())
			.thenReturn(asList(notToBePaid));
		doThrow(new RuntimeException()).when(bankService).pay(anyString(), anyDouble());
		// number of payments must be 0
		assertThat(employeeManager.payEmployees()).isZero();
		// make sure that Employee.paid is updated accordingly
		verify(notToBePaid).setPaid(false);
	}

	@Test
	public void testOtherEmployeesArePaidWhenBankServiceThrowsException() {
		when(employeeRepository.findAll())
			.thenReturn(asList(notToBePaid, toBePaid));
		doThrow(new RuntimeException())
			.doNothing()
			.when(bankService).pay(anyString(), anyDouble());
		// number of payments must be 1
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		// make sure that Employee.paid is updated accordingly
		verify(notToBePaid).setPaid(false);
		verify(toBePaid).setPaid(true);
	}

	@Test
	public void testArgumentMatcherExample() {
		when(employeeRepository.findAll())
			.thenReturn(asList(notToBePaid, toBePaid));
		doThrow(new RuntimeException())
			.when(bankService).pay(
					argThat(s -> s.equals("1")),
					anyDouble());
		// number of payments must be 1
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		// make sure that Employee.paid is updated accordingly
		verify(notToBePaid).setPaid(false);
		verify(toBePaid).setPaid(true);
	}

}
