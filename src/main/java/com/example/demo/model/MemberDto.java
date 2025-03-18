package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberDto {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignupRequest {
        private String userid;
        private String password;

        public Member toEntity(String encodedPassword) {
            return Member.builder()
                    .userid(userid)
                    .password(encodedPassword)
                    .build();
        }
    }

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class SignupResponse{
        private Long idx;
        private String userid;

        public static SignupResponse fromEntity(Member member) {
            return SignupResponse.builder()
                    .idx(member.getIdx())
                    .userid(member.getUserid())
                    .build();
        }
    }
}
