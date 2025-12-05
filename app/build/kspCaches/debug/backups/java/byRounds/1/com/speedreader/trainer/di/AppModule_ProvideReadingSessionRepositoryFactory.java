package com.speedreader.trainer.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.speedreader.trainer.data.remote.OpenAIService;
import com.speedreader.trainer.data.repository.ReadingSessionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideReadingSessionRepositoryFactory implements Factory<ReadingSessionRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> firebaseAuthProvider;

  private final Provider<OpenAIService> openAIServiceProvider;

  public AppModule_ProvideReadingSessionRepositoryFactory(
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider,
      Provider<OpenAIService> openAIServiceProvider) {
    this.firestoreProvider = firestoreProvider;
    this.firebaseAuthProvider = firebaseAuthProvider;
    this.openAIServiceProvider = openAIServiceProvider;
  }

  @Override
  public ReadingSessionRepository get() {
    return provideReadingSessionRepository(firestoreProvider.get(), firebaseAuthProvider.get(), openAIServiceProvider.get());
  }

  public static AppModule_ProvideReadingSessionRepositoryFactory create(
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider,
      Provider<OpenAIService> openAIServiceProvider) {
    return new AppModule_ProvideReadingSessionRepositoryFactory(firestoreProvider, firebaseAuthProvider, openAIServiceProvider);
  }

  public static ReadingSessionRepository provideReadingSessionRepository(
      FirebaseFirestore firestore, FirebaseAuth firebaseAuth, OpenAIService openAIService) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideReadingSessionRepository(firestore, firebaseAuth, openAIService));
  }
}
