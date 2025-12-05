package com.speedreader.trainer.ui.screens.settings;

import com.speedreader.trainer.data.repository.AuthRepository;
import com.speedreader.trainer.data.repository.SettingsRepository;
import com.speedreader.trainer.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get(), userRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider, userRepositoryProvider, settingsRepositoryProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository,
      UserRepository userRepository, SettingsRepository settingsRepository) {
    return new SettingsViewModel(authRepository, userRepository, settingsRepository);
  }
}
