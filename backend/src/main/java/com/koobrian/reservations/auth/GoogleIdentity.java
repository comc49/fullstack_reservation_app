package com.koobrian.reservations.auth;

// Who Google says the person is, after the ID token has been verified.
// hostedDomain is the Workspace domain ("hd" claim), or null for personal Gmail accounts.
public record GoogleIdentity(
    String email,
    String fullName,
    String hostedDomain
) {
}
