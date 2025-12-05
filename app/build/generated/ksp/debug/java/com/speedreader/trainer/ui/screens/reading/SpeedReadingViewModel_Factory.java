package com.speedreader.trainer.ui.screens.reading;

import com.speedreader.trainer.data.repository.DocumentRepository;
import com.speedreader.trainer.data.repository.ReadingSessionRepository;
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
public final class SpeedReadingViewModel_Factory implements Factory<SpeedReadingViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<ReadingSessionRepository> sessionRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SpeedReadingViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider,
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SpeedReadingViewModel get() {
    return newInstance(documentRepositoryProvider.get(), sessionRepositoryProvider.get(), userRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static SpeedReadingViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SpeedReadingViewModel_Factory(documentRepositoryProvider, sessionRepositoryProvider, userRepositoryProvider, settingsRepositoryProvider);
  }

  public static SpeedReadingViewModel newInstance(DocumentRepository documentRepository,
      ReadingSessionRepository sessionRepository, UserRepository userRepository,
      SettingsRepository settingsRepository) {
    return new SpeedReadingViewModel(documentRepository, sessionRepository, userRepository, settingsRepository);
  }
}
