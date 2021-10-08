package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminDto {

    @NotNull(message = "{validation.notNull}")
    private String login;

    @NotNull(message = "{validation.notNull}")
    private String password;

    @Override
    public String toString() {
        return "Login = " + login + ", Password = " + password;
    }
}
