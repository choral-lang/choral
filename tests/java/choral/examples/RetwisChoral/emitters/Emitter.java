package choral.examples.RetwisChoral.emitters;

import choral.examples.RetwisChoral.RetwisAction;
import choral.examples.RetwisChoral.Token;

public interface Emitter {

	Emitter emit( Action action );

	interface Action {
		RetwisAction action();

		default String postsUsername() {
			throw new UnsupportedOperationException();
		}

		default Integer postsPage() {
			throw new UnsupportedOperationException();
		}

		default String post() {
			throw new UnsupportedOperationException();
		}

		default Token sessionToken() {
			throw new UnsupportedOperationException();
		}

		default String followTarget() {
			throw new UnsupportedOperationException();
		}

		default String stopFollowTarget() {
			throw new UnsupportedOperationException();
		}

		default String username() {
			throw new UnsupportedOperationException();
		}

		default String mentionsUsername() {
			throw new UnsupportedOperationException();
		}

		default String statusPostID() {
			throw new UnsupportedOperationException();
		}

		enum Fields {
			postsUsername,
			postsPage,
			post,
			sessionToken,
			followTarget,
			stopFollowTarget,
			username,
			mentionsUsername,
			statusPostID
		}

	}

	class Posts implements Action {
		private final String postsUsername;
		private final Integer postsPage;
		private final RetwisAction action = RetwisAction.POSTS;

		public Posts( String postsUsername, Integer postsPage ) {
			this.postsUsername = postsUsername;
			this.postsPage = postsPage;
		}

		public String postsUsername() {
			return postsUsername;
		}

		public Integer postsPage() {
			return postsPage;
		}

		public RetwisAction action() {
			return action;
		}
	}

	class Post implements Action {
		private final Token sessionToken;
		private final String post;
		private final RetwisAction action = RetwisAction.POST;

		public Post( Token sessionToken, String post ) {
			this.sessionToken = sessionToken;
			this.post = post;
		}

		public Token sessionToken() {
			return sessionToken;
		}

		public String post() {
			return post;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Follow implements Action {
		private final Token sessionToken;
		private final String followTarget;
		private final String username;
		private final RetwisAction action = RetwisAction.FOLLOW;

		public Follow( Token sessionToken, String followTarget, String username ) {
			this.sessionToken = sessionToken;
			this.followTarget = followTarget;
			this.username = username;
		}

		public Token sessionToken() {
			return sessionToken;
		}

		public String followTarget() {
			return followTarget;
		}

		public String username() {
			return username;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class StopFollow implements Action {

		private final Token sessionToken;
		private final String stopFollowTarget;
		private final String username;
		private final RetwisAction action = RetwisAction.STOPFOLLOW;

		public StopFollow( Token sessionToken, String stopFollowTarget, String username ) {
			this.sessionToken = sessionToken;
			this.stopFollowTarget = stopFollowTarget;
			this.username = username;
		}

		public Token sessionToken() {
			return sessionToken;
		}

		public String stopFollowTarget() {
			return stopFollowTarget;
		}

		public String username() {
			return username;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Mentions implements Action {
		private final Token sessionToken;
		private final String mentionsUsername;
		private final RetwisAction action = RetwisAction.MENTIONS;


		public Mentions( Token sessionToken, String mentionsUsername ) {
			this.sessionToken = sessionToken;
			this.mentionsUsername = mentionsUsername;
		}

		public Token sessionToken() {
			return sessionToken;
		}

		public String mentionsUsername() {
			return mentionsUsername;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Status implements Action {
		private final String statusPostID;
		private final RetwisAction action = RetwisAction.STATUS;

		public Status( String statusPostID ) {
			this.statusPostID = statusPostID;
		}

		public String statusPostID() {
			return statusPostID;
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

	class Logout implements Action {
		private final RetwisAction action = RetwisAction.LOGOUT;

		public Logout() {
		}

		@Override
		public RetwisAction action() {
			return action;
		}
	}

}
