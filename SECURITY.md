# Security Policy

## Supported versions

| Version | Supported |
| --- | --- |
| 2.0.x | Yes |
| 1.0.x | No |

## Reporting a vulnerability

Please report privately rather than in a public issue. Use
[GitHub's private advisory form](https://github.com/IMDelewer/LetsTroll/security/advisories/new),
which reaches the maintainer directly and lets the fix land before details are public.

Useful things to include: the plugin and Paper or Fabric versions, the smallest set of
steps that triggers it, and what an attacker gets out of it. A first reply should come
within a few days.

## What is in scope

This plugin does a few things that deserve scrutiny, and reports touching them are
especially welcome:

- **Permission boundaries.** Menus and actions are gated by `letstroll.*` nodes. Anything
  that lets a player reach a screen or action they lack the permission for is a bug.
- **Hiding the plugin.** `hide.plugin` removes LetsTroll from Paper's plugin manager via
  reflection. It is meant to hide the plugin from players, not to defeat server operators
  or other plugins' security assumptions.
- **Outbound requests.** Skin resolution calls `api.mojang.com` and `sessionserver.mojang.com`
  with a validated player name. Anything that lets crafted input redirect those requests
  elsewhere is in scope.
- **Reflection into server internals.** Fake ping and anonymize reach into server classes.
  Reports of these breaking safety guarantees are in scope.

## What is not in scope

- The plugin's intended behaviour. It exists to disrupt players — rubberbanding, fake
  lag, jumpscares and forced teleports are features, and operators grant them deliberately.
- Anything requiring `letstroll.admin` or operator status already. Those permissions are
  full trust by design.
- Server misconfiguration, such as granting `letstroll.use` to everyone.
