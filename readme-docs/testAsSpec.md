# Pragmatic Programming Techniques: "Test" as the "Spec"
One of a frequently encountered question in enterprise software development is where does the hand off happen from the architect (who design the software) to the developer (who implements the software). Usually, the architect designs the software at a higher level of abstraction and then communicate her design to the developers, who break down into more concrete, detail-level design before turning into implementation-level code.

The hand off happens typically via an architecture spec written by the architect, composed of UML class diagrams, sequence diagrams or state transition diagrams ... etc. Based on the understanding from these diagrams, the developers go ahead to write the code.


However, the progression is not as smooth as we expect. There is a gap between the architecture diagrams and the code and a transformation is required. During such transformation, there is a possibility of mis-interpretation, wrong assumption. Quite often, the system ends up to be wrongly implemented due to miscommunication between the developer and the architect. Such miscommunication can either due to the architect hasn't described his design clear enough in the spec, or because the developer is not experienced enough to fill in some left-out detail that the architect consider obvious.

One way to mitigate this problem is to have more design review meetings, or code review session to make sure what is implemented is correctly reflecting the design. Unfortunately, I found such review sessions are usually not happening either because the architect is too busy in other tasks, or she is reluctant to read the developer's code. It ends up the implementation doesn't match the design. Quite often, this discrepancy is discovered at a very late stage and left no time to fix. While developers start patching the current implementation for bug fixing or adding new features, the architect lose the control on the architecture evolution.

Is there a way for the architect to enforce her design at the early stage given the following common constraints ?

1.  The architect cannot afford frequent progress/checkpoint review meetings
2.  While making sure the implementation compliant with the design at a higher level, the architect doesn't want to dictate the low level implementation details

Test As A Specification

The solution is to have the architect writing the Unit Tests (e.g. JUnit Test Classes in Java), which acts as the "Spec" of her design.

In this model, the architect will focus in the "interface" aspect and how this system interact with external parties, such as the client (how this system will be used), as well as the collaborators (how this system uses other systems).

![](https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhixSCE53SBi8Lxi-NlGJeL8BWSnKfBwaZebRzsoLl0hY-CH-YOQHMwwlVIRmk_FfrMq1dUm84EG9CgEQ3fGKe09K7zHClxySdRADGkQJ0D6H7-VbYp3WrnrHqDg635F8FXc0_Y8KL0bb25/s1600-h/P.png)  
The system will expose a set of "Facade" classes which encapsulate the system's external behavior and act as the entry point to its client. Therefore, by writing Unit Tests against these "Facades", the architect can fully specify the external behavior of the system.

A set of "Collaborator" classes is also defined to explicitly capture the interaction of this system to other supporting systems. This "Collaborator" classes are specified in terms of Mock Objects so that the required behavior of supporting system is fully specified. On the other hand, the interaction sequence with the supporting systems are specified via "expectation" of Mock objects.

The architect will specify the behavior of "Facade" and "Collaborator" in a set of XUnit Test Cases, which acts as the design spec of the system. This way, the architect is defining the external behavior of the system while giving enough freedom for the developers to decide on the internal implementation structure. Typically, there are many "Impl Detail" classes which the Facade delegates to. These "Impl Detail" classes will invoke the "Collaborator interface" to get things done in some cases.

Note that the architect is not writing ALL the test cases. Architecture-level Unit Test are just a small subset of the overall test cases specifically focus in the architecture level abstraction. These tests are specifically written to ignore the implementation detail so that its stability will not be affected by change of implementation logic.

On the other hand, developers will also provide a different set of TestCases that covers the "Impl Detail" classes. Usually, this set of "Impl Level TestCase" which change when the developers change the internal implementations. By separating these 2 sets of test cases under different categories, they can evolve independently when different aspects of the system changes along its life cycle, and resulting in a more maintainable system as it evolves.

Example: User Authentication

To illustrate, lets go through an example using a User Authentication system.

There maybe 40 classes that implements this whole UserAuthSubsystem. But the architecture-level test cases only focused in the Facade classes and specify only the "external behavior" of what this subsystem should provide. It doesn't touch any of the underlying implementation classes because those are the implementor's choices which the architect doesn't want to constrain.

\*\* User Authentication Subsystem Spec starts here \*\*

Responsibility:

1.  Register User -- register a new user
2.  Remove User -- delete a registered user
3.  Process User Login -- authenticate a user login and activate a user session
4.  Process User Logout -- inactivate an existing user session

Collaborators:

*   Credit Card Verifier -- Tell if the user name match the the card holder
*   User Database - Store user's login name, password and personal information

```

public class UserAuthSystemTest {
 UserDB mockedUserDB;
 CreditCardVerifier mockedCreditCardVerifier;
 UserAuthSystem uas;
 @Before
 public void setUp() {
   // Setup the Mock collaborators
   mockedUserDB = createMock(UserDB.class);
   mockedCardVerifier =
     createMock(CreditCardVerifier.class);
   uas =
     new UserAuthSubsystem(mockedUserDB,
                       mockedCardVerifier);
 }
 @Test
 public void testUserLogin_withIncorrectPassword() {
   String uName = "ricky";
   String pwd = "test1234";
   // Define the interactions with Collaborators
   expect(mockUserDB.checkPassword(uName, pwd)))
        .andReturn("false");
   replay();
   // Check the external behavior is correct
   assertFalse(uas.login(userName, password));
   assertNull(uas.getLoginSession(userName));
   // Check the collaboration with collaborators
   verify();
 }
 @Test
 public void testRegistration_withGoodCreditCard() {
   String userName = "Ricky TAM";
   String password = "testp";
   String creditCard = "123456781234";
   expect(mockCardVerifier.checkCard(userName,creditCard)))
           .andReturn("true");
   expect(mockUserDB.addUser(userName, password)));
   replay();
   uas.registerUser(userName, creditCard, password));
   verify();
 }
 @Test
 public void testUserLogin_withCorrectPassword() { .... }
 @Test
 public void testRegistration_withBadCreditCard() { .... }
 @Test
 public void testUserLogout() { .... }
 @Test
 public void testUnregisterUser() { .... }
}
```


\*\* User Authentication Subsystem Spec ends here \*\*

Summary

This approach ("Test" as a "Spec") has a number of advantages ...

*   There is no ambiguation about the system's external behavior and hence no room for mis-communication since the intended behavior of the system is communicated clearly in code.
*   The architect can write the TestCase at the level of abstractions she choose. She has full control in what she wants to constraint and what she wants to give freedom.
*   By elevating architect-level test cases as the spec of the system's external behavior. They become more stable and independent of changes in implementation details.
*   This approach force the architect to think repeatedly what is the "interface" of the subsystem and also what are the collaborators. So the system design is forced to have a clean boundary.