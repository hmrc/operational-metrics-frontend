# operational-metrics-frontend ![](https://img.shields.io/github/v/release/hmrc/operational-metrics-frontend)

* [Overview](#overview)
* [Setup](#setup)
* [Development](#development)
* [Internal Auth](#internal-auth)
* [Tests](#tests)
* [License](#license)

## Overview

`operational-metrics-frontend` displays operational metrics supplied by the
[`operational-metrics`](https://github.com/hmrc/operational-metrics) service.

The current page displays service lead-time measurements, including the service,
team, environment, version, slug creation time, first deployment time and lead
time in days. Results can be filtered by team.

The frontend uses `catalogue-wrapper` for the shared Catalogue layout,
navigation and quick search. Access to the application is protected by
Internal Auth.

## Setup

The project is a Scala 3 Play frontend.

A Nix development shell is provided. Enter it using either:

```bash
nix develop
```

or, when using `direnv`:

```bash
direnv allow
```

### Local dependencies

The default local configuration expects the following services:

| Service               | Port | Purpose                                             |
| --------------------- | ---: | --------------------------------------------------- |
| `operational-metrics` | 8863 | Supplies service lead-time data                     |
| `menu-bar`            | 9999 | Supplies Catalogue navigation and quick-search data |
| `internal-auth`       | 8470 | Authorises access to the frontend                   |

The project currently depends on the local snapshot version of
`catalogue-wrapper-play-30`. Publish the wrapper locally before compiling this
frontend:

```bash
cd ../catalogue-wrapper
sbt publishLocal
```

Start the frontend on port `9001` so that it matches the Internal Auth redirect
URL documented below:

```bash
sbt "run 9001"
```

The application is then available at:

```text
http://localhost:9001/operational-metrics-frontend
```

## Development

The main application route is:

```text
GET /operational-metrics-frontend
```

The frontend retrieves service lead-time data from:

```text
GET /operational-metrics/service-lead-times
```

Configuration for local service ports and Catalogue wrapper routes is held in
`conf/application.conf`.

The current service-to-team mapping is temporary and is defined in
`ServiceLeadTimesViewModel`. It should be replaced with Catalogue or
teams-and-repositories data when that integration is available.

Compile the application with:

```bash
sbt clean compile
```

## Internal Auth

The following Internal Auth configuration is required for local development:

| Field              | Contents                                                                                                         |
| ------------------ | ---------------------------------------------------------------------------------------------------------------- |
| Principal          | `operational-metrics-frontend`                                                                                   |
| Redirect URL       | `http://localhost:9001/operational-metrics-frontend/auth/post-sign-in?targetUrl=%2Foperational-metrics-frontend` |
| Resource Type      | `operational-metrics-frontend`                                                                                   |
| Resource Locations | `*`                                                                                                              |
| Action             | `READ`                                                                                                           |

The protected application route requires the `READ` action for resource type
`operational-metrics-frontend` at resource location `*`.

## Tests

Please run the tests with any code changes:

```bash
sbt test
```

To run the test suite with coverage:

```bash
sbt clean coverage test coverageReport
```

## License

This code is open source software licensed under the
[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

